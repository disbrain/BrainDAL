package akkatemplate.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akkatemplate.descriptors.TagDescriptor;
import akkatemplate.messages.*;
import com.disbrain.dbmslayer.DbmsQuery;
import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;
import com.disbrain.dbmslayer.descriptors.RequestModes;
import com.disbrain.dbmslayer.exceptions.DbmsException;
import com.disbrain.dbmslayer.messages.DbmsWorkerCmdReply;
import com.disbrain.dbmslayer.messages.DbmsWorkerCmdRequest;


public class SimpleTestWorker extends UntypedActor {

    private Messages.TestRequest request;
    private Messages.TestReply.Builder response;

    private ActorRef final_dest,
            fetch_data,
            fetch_misc;
    private int all_done = 0;


    public SimpleTestWorker(ActorRef final_dest, Messages.TestRequest message) {
        this.request = message;
        this.final_dest = final_dest;
    }

    @Override
    public void preStart() {


        fetch_data = DbmsQuery.create_generic_fsm(getContext(),
                new QueryGenericArgument(getSelf(),
                        "SELECT GET_LOCK(123,-1);",
                        new RequestModes(RequestModes.RequestTypology.READ_WRITE, RequestModes.RequestBehaviour.RESOURCE_GETTER),
                        true,
                        GetLockReply.class)
        );

        fetch_misc = DbmsQuery.create_generic_fsm(getContext(),
                new QueryGenericArgument(getSelf(),
                        "SELECT Tag_Id, Tag_Name from Tags;",
                        new RequestModes(RequestModes.RequestTypology.READ_ONLY),
                        true,
                        GetTagsReply.class)
        );

        response = Messages.TestReply.newBuilder().setReturnCode(0);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof GetLockReply) {
            GetLockReply reply = (GetLockReply) message;
            if (reply.got_lock == true)
                DbmsQuery.reuse_fsm(fetch_data, //we choose to use async dedicated dispatcher because we got the lock
                        new QueryGenericArgument(getSelf(), //here we could specify another actor for reply destination
                                "DELETE FROM Obj_Info_EN WHERE Obj_Name = ? LIMIT 1;",
                                new RequestModes(RequestModes.RequestTypology.ASYNC_WRITE),
                                false,
                                DeleteReply.class,
                                new Object[]{"LOCUTUS"}
                        )
                );
            return;

        }

        if (message instanceof GetTagsReply) {
            GetTagsReply reply = (GetTagsReply) message;
            getContext().stop(getSender());
            if (reply.output != null)
                for (TagDescriptor element : reply.output)
                    response.addOutData(element.tag_id);
            if (++all_done == 2) //2, the number of pending exec streams
            {
                final_dest.tell(response.build(), ActorRef.noSender());
                getContext().stop(getSelf());
            }
            return;
        }

        if (message instanceof DeleteReply) {
            DbmsQuery.async_reuse_fsm(getSender(), new QueryGenericArgument(getSelf(),
                    "SELECT RELEASE_LOCK(123);",
                    new RequestModes(RequestModes.RequestTypology.READ_WRITE,
                            RequestModes.RequestBehaviour.RESOURCE_RELEASER),
                    false, //don't commit
                    ReleaseReply.class
            )
            );
            return;
        }

        if (message instanceof ReleaseReply) {
            /* since we have not committed yet, we can force it talking directly to the Dbmslayer if we want */
            fetch_data.tell(new DbmsWorkerCmdRequest(DbmsWorkerCmdRequest.Command.COMMIT), getSelf());
            return;
        }

        if (message instanceof DbmsWorkerCmdReply) {
            DbmsWorkerCmdReply reply = (DbmsWorkerCmdReply) message;
            if (reply.originalRequest == DbmsWorkerCmdRequest.Command.COMMIT) // fine, commit done!
            {
                if (++all_done == 2) {
                    final_dest.tell(response.build(), ActorRef.noSender());
                    getContext().stop(getSelf());
                }
                return;
            }
        }

        /* unhandled */
        response.setReturnCode(-1);

        if (message instanceof DbmsException) {
            DbmsException error = (DbmsException) message;
            /* here we can intercept every error coming from dbmslayer */
            response.clear().setReturnCode(error.getErrorCode()).setReturnMsg(error.getMessage());
        }
        final_dest.tell(response.build(), ActorRef.noSender());
        getContext().stop(getSelf());
    }
}
