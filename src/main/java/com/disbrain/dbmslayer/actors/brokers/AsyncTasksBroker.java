package com.disbrain.dbmslayer.actors.brokers;

import akka.actor.UntypedActor;
import com.disbrain.dbmslayer.descriptors.RequestModes;
import com.disbrain.dbmslayer.exceptions.DbmsLayerError;
import com.disbrain.dbmslayer.exceptions.DbmsRemoteDbError;
import com.disbrain.dbmslayer.messages.DBMSReply;
import com.disbrain.dbmslayer.messages.DbmsExecutePrepStmtRequest;
import com.disbrain.dbmslayer.messages.DbmsExecuteStmtRequest;

import java.sql.SQLException;


public class AsyncTasksBroker extends UntypedActor {

    /* AN ASYNCER RELEASER CAN  -NEVER- BLOCK */
    /* AN ASYNCER GETTER IS ALMOST ALWAYS BLOCKED */

    @Override
    public void onReceive(Object message) {
        Object output_data = null;

        do {

            try {

                if (message instanceof DbmsExecutePrepStmtRequest) {

                    DbmsExecutePrepStmtRequest exec_msg = (DbmsExecutePrepStmtRequest) message;
                    exec_msg.stmt.getConnection().setAutoCommit(exec_msg.autocommit);
                    switch (exec_msg.request_opts.typology) {
                        case ASYNC_WRITE:
                            output_data = new DBMSReply(exec_msg.stmt.executeUpdate(), exec_msg.request_opts.typology);
                            break;
                        case ASYNC_READ_ONLY:
                        case ASYNC_READ_WRITE:
                            output_data = new DBMSReply(exec_msg.stmt.executeQuery(), exec_msg.request_opts.typology);
                            break;
                        default:
                            output_data = new DbmsLayerError("Unhandled Request typology: " + message.getClass());
                            continue;

                    }

                    break;
                }

                if (message instanceof DbmsExecuteStmtRequest) {

                    DbmsExecuteStmtRequest exec_msg = (DbmsExecuteStmtRequest) message;
                    exec_msg.stmt.getConnection().setAutoCommit(exec_msg.autocommit);
                    switch (exec_msg.request_opts.typology) {
                        case ASYNC_WRITE:
                            output_data = new DBMSReply(exec_msg.stmt.executeUpdate(exec_msg.query), RequestModes.RequestTypology.WRITE);
                            break;
                        case ASYNC_READ_ONLY:
                            output_data = new DBMSReply(exec_msg.stmt.executeQuery(exec_msg.query), RequestModes.RequestTypology.READ_ONLY);
                            break;
                        case ASYNC_READ_WRITE:
                            output_data = new DBMSReply(exec_msg.stmt.executeQuery(exec_msg.query), RequestModes.RequestTypology.READ_WRITE);
                            break;
                        default:
                            output_data = new DbmsLayerError("Unhandled Request typology: " + message.getClass());
                            continue;
                    }

                    break;
                }
            } catch (SQLException exc) {
                output_data = new DbmsRemoteDbError(exc);
                break;
            }
            output_data = new DbmsLayerError("Unhandled message: " + message.getClass());
        }
        while (false);
        getSender().tell(output_data, getSelf());
    }
}
