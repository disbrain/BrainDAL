package com.disbrain.dbmslayer;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.actors.brokers.AsyncTasksBroker;
import com.disbrain.dbmslayer.actors.brokers.ConnectionsBroker;
import com.disbrain.dbmslayer.actors.brokers.DeadLetterBroker;
import com.disbrain.dbmslayer.actors.brokers.GenericQueryBroker;
import com.disbrain.dbmslayer.net.DbmsConnectionPool;
import com.typesafe.config.Config;


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

    public enum DeathPolicy {SUICIDE, SURVIVE}

    private DbmsConnectionPool setup_configured_pool(LoggingAdapter logger, Config config) throws Exception {
        Config user_cfg = config.getConfig("DbmsLayer.akka.connection-pool");
        Class pool_provider_class = getClass().getClassLoader().loadClass(user_cfg.getString("pool_connector_class"));
        DbmsConnectionPool pool = (DbmsConnectionPool) pool_provider_class.getConstructor(LoggingAdapter.class).newInstance(logger);
        pool.setJdbcUrl(
                String.format("jdbc:mysql://%s:%d/%s",
                        user_cfg.getString("mysqldb.host"),
                        user_cfg.getInt("mysqldb.port"),
                        user_cfg.getString("mysqldb.dbname"))
        )
                .setDriver(user_cfg.getString("driver"))
                .setUsername(user_cfg.getString("mysqldb.username"))
                .setPassword(user_cfg.getString("mysqldb.pwd"))
                .setStatementsCacheSize(user_cfg.getInt("stmt-cachesize"))
                .setAutoCommit(user_cfg.getBoolean("autocommit"))
                .setDefaultTransactionIsolation(user_cfg.getString("trans-isolation-level"))
                .setMinimumConnectionNum(user_cfg.getInt("min-partition-connections"))
                .setMaximumConnectionNum(user_cfg.getInt("max-partition-connections"))
                .setConnectionAcquireIncrement(user_cfg.getInt("connections-acquire-increment"))
                .setPartitionsNum(user_cfg.getInt("partitions-count"))
                .setIdleConnectionTestPeriod(user_cfg.getInt("idle-test-period"))
                .setStatistics(user_cfg.getBoolean("statistics"))
                .setPoolAvailabilityThreshold(user_cfg.getInt("pool-availability-threshold"))
                .setConnectionLeakWatch(user_cfg.getLong("close-connection-watch-timeout"))
                .setMaxConnectionAge(user_cfg.getInt("max-connection-age"))
                .setLogLevel(user_cfg.getString("pool_loglevel"))
                .setInitPolitic(!user_cfg.getBoolean("shall-init-pool"))
                .setConnectionTimeout(user_cfg.getInt("connection-timeout"))
                .createPool();
        return pool;
    }


    public DbmsLayerProvider(ActorSystem fatherAS) throws Exception {

        system = fatherAS;
        dbmslayer_log = Logging.getLogger(system, this);
        async_get_tasks_broker = system.actorOf(Props.create(AsyncTasksBroker.class).withDispatcher("DbmsLayer.akka.actor.async-get-broker-dispatcher"), "ASYNC_GET_TASK_BROKER");
        async_release_tasks_broker = system.actorOf(Props.create(AsyncTasksBroker.class).withDispatcher("DbmsLayer.akka.actor.async-release-broker-dispatcher"), "ASYNC_RELEASE_TASK_BROKER");
        async_whatever_tasks_broker = system.actorOf(Props.create(AsyncTasksBroker.class).withDispatcher("DbmsLayer.akka.actor.async-whatever-broker-dispatcher"), "ASYNC_WHATEVER_TASK_BROKER");
        connections_broker = system.actorOf(Props.create(ConnectionsBroker.class, setup_configured_pool(dbmslayer_log, system.settings().config())).withDispatcher("DbmsLayer.akka.actor.connections-dispatcher"), "CONNECTIONS_BROKER");
        close_connections_broker = system.actorOf(Props.create(ConnectionsBroker.class, dbmslayer_log).withDispatcher("DbmsLayer.akka.actor.close-connections-dispatcher"), "CLOSE_CONNECTIONS_BROKER");
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
