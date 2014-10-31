package com.disbrain.dbmslayer.actors.brokers;

import akka.actor.ActorRef;
import akka.actor.DeadLetter;
import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.DbmsLayer;
import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;
import com.disbrain.dbmslayer.exceptions.DbmsException;
import com.disbrain.dbmslayer.messages.CloseDbmsConnectionRequest;
import com.disbrain.dbmslayer.messages.GetDbmsConnectionReply;
import com.disbrain.dbmslayer.messages.GetDbmsPreparedStatementReply;
import com.disbrain.dbmslayer.messages.GetDbmsStatementReply;

import java.sql.SQLException;

public class DeadLetterBroker extends UntypedActor {


    private ActorRef connection_broker;
    private final LoggingAdapter log;

    public DeadLetterBroker(ActorRef connection_broker) {
        log = DbmsLayer.DbmsLayerProvider.get(getContext().system()).getLoggingAdapter();
        this.connection_broker = connection_broker;
    }

    @Override
    public void onReceive(Object message) {
        do {

            if (message instanceof DeadLetter) {
                DeadLetter dead_msg = (DeadLetter) message;
                Object real_msg;
                real_msg = dead_msg.message();

                if (real_msg instanceof GetDbmsConnectionReply)
                {
                    connection_broker.tell(new CloseDbmsConnectionRequest(((GetDbmsConnectionReply) real_msg).connection), ActorRef.noSender());
                    return;
                }

                if(real_msg instanceof QueryGenericArgument)
                {
                    log.error("GenericQueryArgument found! Are you trying to reuse a suicided GenericDBMSWorker?");
                    return;
                }
                try {
                    if (real_msg instanceof GetDbmsStatementReply) {
                        GetDbmsStatementReply reply = (GetDbmsStatementReply) real_msg;

                        reply.stmt.close();
                        return;
                    }
                    if (real_msg instanceof GetDbmsPreparedStatementReply) {
                        GetDbmsPreparedStatementReply reply = (GetDbmsPreparedStatementReply) real_msg;
                        reply.p_stmt.close();
                        return;
                    }
                } catch (SQLException exc) {
                    log.error(exc, "Unexpected error while closing and orphaned statement");
                    return;
                }

                break;
            }

            if (message instanceof DbmsException) {
                DbmsException msg = (DbmsException) message;
                log.error("ERROR IN DEAD LETTER DISPATCHER!!!\nError code: " + msg.getErrorCode() + "\nMessage: " + msg.getMessage());
                break;
            }

        } while (false);
    }

}
