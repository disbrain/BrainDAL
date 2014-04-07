package com.disbrain.dbmslayer;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.actors.brokers.AsyncTasksBroker;
import com.disbrain.dbmslayer.actors.brokers.ConnectionsBroker;
import com.disbrain.dbmslayer.actors.brokers.GenericQueryBroker;
import com.disbrain.dbmslayer.actors.brokers.DeadLetterBroker;
import com.disbrain.dbmslayer.net.DbmsConnectionPool;
import com.typesafe.config.Config;

import java.beans.PropertyVetoException;
import java.sql.SQLException;


public class DbmsLayerProvider implements Extension {

    private final ActorSystem system;
    private final ActorRef async_get_tasks_broker;
    private final ActorRef async_release_tasks_broker;
    private final ActorRef async_whatever_tasks_broker;
    private final ActorRef connections_broker;
    private final ActorRef close_connections_broker;
    private final ActorRef dead_letter_handler;
    private final ActorRef dbms_queries_broker;
    private final LoggingAdapter dbmslayer_log;

    public enum DeathPolicy {SUICIDE, SURVIVE};

    private DbmsConnectionPool create_configured_pool(LoggingAdapter logger, Config config) throws SQLException, PropertyVetoException, ClassNotFoundException {
        DbmsConnectionPool pool = null;
        Config user_cfg = config.getConfig("DbmsLayer.akka.connection-pool");
        pool = new DbmsConnectionPool(logger, user_cfg.getString("pool"), user_cfg.getString("driver"));
        if (user_cfg.getBoolean("shall-init-pool") == true)
        {
            pool = pool.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", user_cfg.getString("mysqldb.host"),
                user_cfg.getInt("mysqldb.port"), user_cfg.getString("mysqldb.dbname")))
                .setUsername(user_cfg.getString("mysqldb.username")).setPassword(user_cfg.getString("mysqldb.pwd"))
                .setStatementsCacheSize(user_cfg.getInt("stmt-cachesize")).setDefaultAutoCommit(user_cfg.getBoolean("autocommit"))
                .setDefaultTransactionIsolation(user_cfg.getString("trans-isolation-level"))
                .setMinConnectionsPerPartition(user_cfg.getInt("min-partition-connections"))
                .setMaxConnectionsPerPartition(user_cfg.getInt("max-partition-connections"))
                .setAcquireIncrement(user_cfg.getInt("connections-acquire-increment"))
                .setPartitionCount(user_cfg.getInt("partitions-count"))
                .setIdleConnectionTestPeriodInSeconds(user_cfg.getInt("idle-test-period"))
                .setStatisticsEnabled(user_cfg.getBoolean("statistics"))
                .setPoolAvailabilityThreshold(user_cfg.getInt("pool-availability-threshold"))
                .setDisableConnectionTracking(user_cfg.getBoolean("disable-connection-tracking"))
                .setCloseConnectionWatch(user_cfg.getBoolean("close-connection-watch"))
                .setMaxConnectionAgeInSeconds(user_cfg.getInt("max-connection-age"))
                .createPool();
        }
        return pool;
    }

    public DbmsLayerProvider(ActorSystem fatherAS) throws PropertyVetoException, SQLException, ClassNotFoundException {

        system = fatherAS;
        dbmslayer_log = Logging.getLogger(system, this);
        async_get_tasks_broker = system.actorOf(Props.create(AsyncTasksBroker.class).withDispatcher("DbmsLayer.akka.actor.async-get-broker-dispatcher"), "ASYNC_GET_TASK_BROKER");
        async_release_tasks_broker = system.actorOf(Props.create(AsyncTasksBroker.class).withDispatcher("DbmsLayer.akka.actor.async-release-broker-dispatcher"), "ASYNC_RELEASE_TASK_BROKER");
        async_whatever_tasks_broker = system.actorOf(Props.create(AsyncTasksBroker.class).withDispatcher("DbmsLayer.akka.actor.async-whatever-broker-dispatcher"), "ASYNC_WHATEVER_TASK_BROKER");
        connections_broker = system.actorOf(Props.create(ConnectionsBroker.class, create_configured_pool(dbmslayer_log, system.settings().config())).withDispatcher("DbmsLayer.akka.actor.connections-dispatcher"), "CONNECTIONS_BROKER");
        close_connections_broker = system.actorOf(Props.create(ConnectionsBroker.class).withDispatcher("DbmsLayer.akka.actor.close-connections-dispatcher"), "CLOSE_CONNECTIONS_BROKER");
        dbms_queries_broker = system.actorOf(Props.create(GenericQueryBroker.class).withDispatcher("DbmsLayer.akka.actor.dbms-queries-dispatcher"), "QUERIES_BROKER");
        dead_letter_handler = system.actorOf(Props.create(DeadLetterBroker.class, getCloseConnectionsBroker()).withDispatcher("DbmsLayer.akka.actor.deadletters-dispatcher"), "DEADLETTER_HANDLER");
        system.eventStream().subscribe(dead_letter_handler, DeadLetter.class);
    }

    public ActorRef getAsyncGetTasksBroker() {
        return async_get_tasks_broker;
    }

    public ActorRef getAsyncReleaseTasksBroker() {
        return async_release_tasks_broker;
    }

    public ActorRef getAsyncWhateverTasksBroker() {
        return async_whatever_tasks_broker;
    }

    public ActorRef getConnectionsBroker() {
        return connections_broker;
    }

    public ActorRef getCloseConnectionsBroker() {
        return close_connections_broker;
    }

    public ActorRef getQueriesBroker() {
        return dbms_queries_broker;
    }

    public LoggingAdapter getLoggingAdapter() {
        return dbmslayer_log;
    }

    public ActorSystem getActorSystem() {
        return system;
    }

}
