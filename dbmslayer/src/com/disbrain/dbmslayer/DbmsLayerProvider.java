package com.disbrain.dbmslayer;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.actors.brokers.AsyncTasksBroker;
import com.disbrain.dbmslayer.actors.brokers.ConnectionsBroker;
import com.disbrain.dbmslayer.actors.brokers.DeadLetterBroker;
import com.disbrain.dbmslayer.actors.brokers.GenericQueryBroker;
import com.disbrain.dbmslayer.exceptions.DbmsLayerError;
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
        Config user_cfg;
        Class pool_provider_class;
        DbmsConnectionPool pool;
        boolean source_set = false;

        if (config.hasPath("DbmsLayer.akka.connection-pool") == false)
            throw new DbmsLayerError("Missing connection pool section in config file");

        user_cfg = config.getConfig("DbmsLayer.akka.connection-pool");

        if (user_cfg.hasPath("pool_connector_class") == false)
            throw new DbmsLayerError("Missing connection pool class in config file");

        pool_provider_class = getClass().getClassLoader().loadClass(user_cfg.getString("pool_connector_class"));
        pool = (DbmsConnectionPool) pool_provider_class.getConstructor(LoggingAdapter.class).newInstance(logger);

        if (user_cfg.hasPath("driver") == true) {
            pool.setDriver(user_cfg.getString("driver"));
            source_set = true;
        }

        if (user_cfg.hasPath("data-source") == true) {
            pool.setDataSource(user_cfg.getString("data-source"));
            source_set = true;
        }

        if (source_set == false)
            throw new DbmsLayerError("You must set at least a driver or a data source");

        if (user_cfg.hasPath("mysqldb.host") == false)
            throw new DbmsLayerError("Missing database host value");
        if (user_cfg.hasPath("mysqldb.port") == false)
            throw new DbmsLayerError("Missing database port value");
        if (user_cfg.hasPath("mysqldb.dbname") == false)
            throw new DbmsLayerError("Missing database name value");

        pool.setJdbcUrl(
                        user_cfg.getString("mysqldb.host"),
                        user_cfg.getInt("mysqldb.port"),
                user_cfg.getString("mysqldb.dbname")
        );

        if (user_cfg.hasPath("mysqldb.username") == false)
            throw new DbmsLayerError("Missing database username");
        if (user_cfg.hasPath("mysqldb.password") == false)
            throw new DbmsLayerError("Missing database password");


        pool.setUsername(user_cfg.getString("mysqldb.username"))
                .setPassword(user_cfg.getString("mysqldb.password"));

        if (user_cfg.hasPath("autocommit") == true)
            pool.setAutoCommit(user_cfg.getBoolean("autocommit"));
        else
            pool.setAutoCommit(true);

        if (user_cfg.hasPath("trans-isolation-level") == false)
            throw new DbmsLayerError("Missing default transaction level");

        pool.setDefaultTransactionIsolation(user_cfg.getString("trans-isolation-level"));

        if (user_cfg.hasPath("min-connections") == true)
            pool.setMinimumConnectionNum(user_cfg.getInt("min-connections"));

        if (user_cfg.hasPath("max-connections") == true)
            pool.setMaximumConnectionNum(user_cfg.getInt("max-connections"));

        if (user_cfg.hasPath("connections-acquire-increment"))
            pool.setConnectionAcquireIncrement(user_cfg.getInt("connections-acquire-increment"));

        if (user_cfg.hasPath("partitions-count"))
            pool.setPartitionsNum(user_cfg.getInt("partitions-count"));

        if (user_cfg.hasPath("idle-test-period"))
            pool.setIdleConnectionTestPeriod(user_cfg.getInt("idle-test-period"));

        if (user_cfg.hasPath("statistics-enabled"))
            pool.setStatistics(user_cfg.getBoolean("statistics"));

        if (user_cfg.hasPath("pool-availability-threshold"))
            pool.setPoolAvailabilityThreshold(user_cfg.getInt("pool-availability-threshold"));

        if (user_cfg.hasPath("close-connection-watch-timeout"))
            pool.setConnectionLeakWatch(user_cfg.getLong("close-connection-watch-timeout"));

        if (user_cfg.hasPath("max-connection-age"))
            pool.setMaxConnectionAge(user_cfg.getInt("max-connection-age"));

        if (user_cfg.hasPath("pool-loglevel"))
            pool.setLogLevel(user_cfg.getString("pool-loglevel"));

        if (user_cfg.hasPath("shall-init-pool"))
            pool.setInitPolitic(!user_cfg.getBoolean("shall-init-pool"));

        if (user_cfg.hasPath("prepstmt-cache-size"))
            pool.setPrepStmtCacheSize(user_cfg.getInt("prepstmt-cache-size"));

        if (user_cfg.hasPath("connection-timeout"))
            pool.setConnectionTimeout(user_cfg.getInt("connection-timeout"));

        pool.createPool();
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
