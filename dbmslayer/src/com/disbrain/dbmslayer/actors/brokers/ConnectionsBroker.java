package com.disbrain.dbmslayer.actors.brokers;

import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.DbmsLayer;
import com.disbrain.dbmslayer.exceptions.DbmsConnectionPoolError;
import com.disbrain.dbmslayer.messages.CloseDbmsConnectionRequest;
import com.disbrain.dbmslayer.messages.GetDbmsConnectionReply;
import com.disbrain.dbmslayer.messages.GetDbmsConnectionRequest;
import com.disbrain.dbmslayer.net.DbmsConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;


public class ConnectionsBroker extends UntypedActor {

    private DbmsConnectionPool connection_pool = null;
    private static volatile AtomicInteger connections_taken = new AtomicInteger(0);
    private DbmsConnectionPoolError error = new DbmsConnectionPoolError();
    private final LoggingAdapter log;

    public static int getBrokerStats() {
        return connections_taken.get();
    }

    public ConnectionsBroker(DbmsConnectionPool pool) {
        connection_pool = pool;
        log = DbmsLayer.DbmsLayerProvider.get(getContext().system()).getLoggingAdapter();
    }

    public ConnectionsBroker() {
        log = DbmsLayer.DbmsLayerProvider.get(getContext().system()).getLoggingAdapter();
    }

    @Override
    public void onReceive(Object message) {
        do {
            if (message instanceof GetDbmsConnectionRequest) {

                Object output = null;

                try {
                    output = new GetDbmsConnectionReply(connection_pool.getConnection(), ((GetDbmsConnectionRequest) message).connection_params);
                    connections_taken.incrementAndGet();
                } catch (SQLException exc) {
                    output = new DbmsConnectionPoolError(exc, connection_pool.getStatistics());
                }
                getSender().tell(output, getSelf());

                break;
            }

            if (message instanceof CloseDbmsConnectionRequest) {

                Connection close_this = ((CloseDbmsConnectionRequest) message).connection;

                try {
                    if (close_this.isClosed()) {
                        log.debug("CANNOT CLOSE OR ROLLBACK A CLOSED CONNECTION!");
                        return;
                    }
                } catch (SQLException exc) {
                    log.error(exc, "ERROR CHECKING CONNECTION STATUS, TRYING TO KEEP GOING");
                }
                try {
                    if (close_this.getAutoCommit() == false)
                        close_this.rollback();
                } catch (SQLException exc) {
                    log.error(exc, "ERROR ROLLBACKING A CONNECTION, TRYING TO KEEP GOING");
                }
                try {
                    close_this.close();
                    connections_taken.decrementAndGet();
                } catch (SQLException exc) {
                    log.error(exc, "CRITICAL ERROR CLOSING A CONNECTION!");
                }
                break;
            }

            getSender().tell(new DbmsConnectionPoolError("Unknown message: " + message.getClass()), getSelf());

        } while (false);


    }
}
