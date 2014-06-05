package com.disbrain.dbmslayer.actors.brokers;

import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.exceptions.DbmsConnectionPoolError;
import com.disbrain.dbmslayer.messages.CloseDbmsConnectionRequest;
import com.disbrain.dbmslayer.messages.GetDbmsConnectionReply;
import com.disbrain.dbmslayer.messages.GetDbmsConnectionRequest;
import com.disbrain.dbmslayer.net.DbmsConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;


public class ConnectionsBroker extends UntypedActor {

    private static class ConnectionStats {

        private volatile AtomicInteger connections_taken = new AtomicInteger(0);
        private volatile AtomicInteger errors_taking = new AtomicInteger(0);
        private volatile AtomicInteger errors_releasing = new AtomicInteger(0);
        private volatile AtomicInteger totally_used = new AtomicInteger(0);

        public void gotConnection() {
            totally_used.incrementAndGet();
            connections_taken.incrementAndGet();
        }

        public void gotError() {
            errors_taking.incrementAndGet();
        }

        public void releaseConnection() {
            connections_taken.decrementAndGet();
        }

        public void releaseError() {
            errors_releasing.incrementAndGet();
        }

        public long getTaken() {
            return connections_taken.get();
        }

        public long getTotalUsage() {
            return totally_used.get();
        }

        public long getTakeErrors() {
            return errors_taking.get();
        }

        public long getReleaseErrors() {
            return errors_releasing.get();
        }

    }

    ;

    private final DbmsConnectionPool connection_pool;
    private final static ConnectionStats stats = new ConnectionStats();
    private final LoggingAdapter log;

    public static String getBrokerStats() {
        return String.format("Currently taken: %d Get errors: %d Release errors: %d Total usage: %d\n",
                stats.getTaken(),
                stats.getTakeErrors(),
                stats.getReleaseErrors(),
                stats.getTotalUsage());
    }

    public ConnectionsBroker(DbmsConnectionPool pool) {
        connection_pool = pool;
        log = connection_pool.getLogger();
    }

    public ConnectionsBroker(LoggingAdapter logger) {
        log = logger;
        connection_pool = null;
    }

    @Override
    public void onReceive(Object message) {

        do {
            if (message instanceof GetDbmsConnectionRequest) {

                Object output;

                try {
                    Connection connection = connection_pool.getConnection();
                    if (connection == null) //Timeout while getting a connection
                        throw new SQLException("Timeout while trying to fetch a connection!");
                    output = new GetDbmsConnectionReply(connection_pool.getConnection(), ((GetDbmsConnectionRequest) message).connection_params);
                    stats.gotConnection();
                } catch (Exception exc) {
                    stats.gotError();
                    output = new DbmsConnectionPoolError(exc, getBrokerStats());
                }
                log.debug("Get stats: " + getBrokerStats());
                getSender().tell(output, getSelf());

                break;
            }

            if (message instanceof CloseDbmsConnectionRequest) {

                Connection close_this = ((CloseDbmsConnectionRequest) message).connection;

                try {
                    if (close_this.isClosed()) {
                        stats.gotError();
                        log.error("CANNOT CLOSE OR ROLLBACK A CLOSED CONNECTION!");
                        return;
                    }
                } catch (SQLException exc) {
                    stats.gotError();
                    log.error(exc, "ERROR CHECKING CONNECTION STATUS, TRYING TO KEEP GOING");
                }
                try {
                    if (close_this.getAutoCommit() == false)
                        close_this.rollback();
                } catch (SQLException exc) {
                    stats.gotError();
                    log.error(exc, "ERROR ROLLBACKING A CONNECTION, TRYING TO KEEP GOING");
                }
                try {
                    close_this.close();
                    stats.releaseConnection();
                } catch (Exception exc) {
                    stats.gotError();
                    log.error(exc, "CRITICAL ERROR CLOSING A CONNECTION!");
                }
                log.debug("Close status: " + getBrokerStats());
                break;
            }
            log.error("UNKNOWN: " + message.getClass().getName());
            getSender().tell(new DbmsConnectionPoolError("Unknown message: " + message.getClass().getName()), getSelf());

        } while (false);


    }
}
