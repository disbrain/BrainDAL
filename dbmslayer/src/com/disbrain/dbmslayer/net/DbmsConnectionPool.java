package com.disbrain.dbmslayer.net;

import akka.event.LoggingAdapter;

import java.sql.Connection;


public interface DbmsConnectionPool {

    public DbmsConnectionPool createPool() throws Exception;

    public DbmsConnectionPool setJdbcUrl(String host, int port, String dbname);

    public DbmsConnectionPool setUsername(String username);

    public DbmsConnectionPool setPassword(String password);

    public DbmsConnectionPool setAutoCommit(boolean value);

    public DbmsConnectionPool setDefaultTransactionIsolation(String isolation_level);

    public DbmsConnectionPool setMinimumConnectionNum(int limit);

    public DbmsConnectionPool setMaximumConnectionNum(int limit);

    public DbmsConnectionPool setConnectionAcquireIncrement(int count);

    public DbmsConnectionPool setPartitionsNum(int num);

    public DbmsConnectionPool setIdleConnectionTestPeriod(int seconds);

    public DbmsConnectionPool setStatistics(boolean value);

    public DbmsConnectionPool setPoolAvailabilityThreshold(int percent);

    public DbmsConnectionPool setConnectionLeakWatch(long msec_timeout);

    public DbmsConnectionPool setMaxConnectionAge(int seconds);

    public DbmsConnectionPool setLogLevel(String loglevel);

    public DbmsConnectionPool setInitPolitic(boolean lazyness);

    public DbmsConnectionPool setConnectionTimeout(long seconds);

    public DbmsConnectionPool setPrepStmtCacheSize(int cache_size);

    public DbmsConnectionPool setDriver(String driver);

    public DbmsConnectionPool setDataSource(String dataSource);

    public LoggingAdapter getLogger();


    public Connection getConnection() throws Exception;

    public String getStatistics();

}
