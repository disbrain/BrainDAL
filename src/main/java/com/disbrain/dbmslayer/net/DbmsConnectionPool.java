package com.disbrain.dbmslayer.net;

import akka.actor.ActorSystem;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.DbmsLayer;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.Statistics;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;


public class DbmsConnectionPool {


    /* Currently supported Connection Pools: C3P0 and BoneCP */
    public static enum CP {
        BoneCP, C3P0
    }

    ;

    private CP pool_mode = null;

    private volatile ComboPooledDataSource c3po_ds = null;

    private BoneCPConfig bonecp_cfg = null;
    private volatile BoneCP bone_cp = null;
    private final LoggingAdapter log;

    public DbmsConnectionPool(ActorSystem system) {
        log = DbmsLayer.DbmsLayerProvider.get(system).getLoggingAdapter();
    }

    public CP getCP(String desc) {
        do {
            if (desc != null) {
                if (desc.equalsIgnoreCase("c3p0"))
                    return CP.C3P0;
                if (desc.equalsIgnoreCase("bonecp"))
                    return CP.BoneCP;
            }
            log.info("Unknown connection pool, defaulting to C3P0");
        } while (false);
        return CP.C3P0; //default
    }

    public DbmsConnectionPool setJdbcUrl(String link) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setJdbcUrl(link);
                break;
            case C3P0:
                c3po_ds.setJdbcUrl(link);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setUsername(String username) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setUsername(username);
                break;
            case C3P0:
                c3po_ds.setUser(username);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setPassword(String password) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setPassword(password);
                break;
            case C3P0:
                c3po_ds.setPassword(password);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setStatementsCacheSize(int size) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setStatementsCacheSize(size);
                break;
            case C3P0:
                c3po_ds.setMaxStatements(size);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setDefaultAutoCommit(boolean value) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setDefaultAutoCommit(value);
                break;
            case C3P0:
                c3po_ds.setAutoCommitOnClose(value);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setDefaultTransactionIsolation(String isolation_level) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setDefaultTransactionIsolation(isolation_level);
                break;
            case C3P0:
                log.info("Not implemented for C3P0, using C3poPrefs instead");
                c3po_ds.setConnectionCustomizerClassName("com.disbrain.dbmslayer.net.C3poPrefs");
                break;
        }
        return this;
    }

    public DbmsConnectionPool setMinConnectionsPerPartition(int limit) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setMinConnectionsPerPartition(limit);
                break;
            case C3P0:
                c3po_ds.setMinPoolSize(limit);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setMaxConnectionsPerPartition(int limit) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setMaxConnectionsPerPartition(limit);
                break;
            case C3P0:
                c3po_ds.setMaxPoolSize(limit);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setAcquireIncrement(int increment) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setAcquireIncrement(increment);
                break;
            case C3P0:
                c3po_ds.setAcquireIncrement(increment);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setPartitionCount(int count) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setPartitionCount(count);
                break;
            case C3P0:
                log.info("Partitions logic not implemented for C3P0");
                break;
        }
        return this;
    }

    public DbmsConnectionPool setIdleConnectionTestPeriodInSeconds(int period) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setIdleConnectionTestPeriodInSeconds(period);
                break;
            case C3P0:
                c3po_ds.setIdleConnectionTestPeriod(period);
                break;
        }
        return this;
    }

    public DbmsConnectionPool setStatisticsEnabled(boolean value) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setStatisticsEnabled(value);
                break;
            case C3P0:
                log.info("Statistics not implemented for C3P0");
                break;
        }
        return this;
    }

    public DbmsConnectionPool setPoolAvailabilityThreshold(int threshold) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setPoolAvailabilityThreshold(threshold);
                break;
            case C3P0:
                log.info("Threshold not implemented with C3P0");
                break;
        }
        return this;
    }

    public DbmsConnectionPool setDisableConnectionTracking(boolean value) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setDisableConnectionTracking(value);
                break;
            case C3P0:
                log.info("Connection tracking not implemented in C3P0");
                break;
        }
        return this;
    }

    public DbmsConnectionPool setCloseConnectionWatch(boolean value) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setCloseConnectionWatch(value);
                break;
            case C3P0:
                int val = 3600;
                if (value == true)
                    val = 0;
                c3po_ds.setUnreturnedConnectionTimeout(val);
                break;
        }
        return this;
    }

    public String getStatistics() {
        String retVal = "CacheHitRatio: %f CacheHit: %d CacheMiss: %d\nConnectionsRequested: %d ConnectionWaitTimeAvg: %f\nStatementsCached: %d StatementsPrepared: %d\nCreatedConnections: %d TotalFree: %d TotalLeased: %d";
        switch (pool_mode) {
            case BoneCP:
                Statistics stats = bone_cp.getStatistics();
                retVal = String.format(retVal, stats.getCacheHitRatio(), stats.getCacheHits(), stats.getCacheMiss(), stats.getConnectionsRequested(), stats.getConnectionWaitTimeAvg(), stats.getStatementsCached(), stats.getStatementsPrepared(), stats.getTotalCreatedConnections(), stats.getTotalFree(), stats.getTotalLeased());
                break;
        }

        return retVal;
    }

    public DbmsConnectionPool createPool() throws SQLException {
        switch (pool_mode) {
            case BoneCP:
                bone_cp = new BoneCP(bonecp_cfg);
                break;
            case C3P0:
                c3po_ds.setInitialPoolSize(c3po_ds.getMinPoolSize());
                break;

        }
        return this;
    }

    public DbmsConnectionPool setMaxConnectionAgeInSeconds(int seconds) {
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg.setMaxConnectionAgeInSeconds(seconds);
                break;
            case C3P0:
                c3po_ds.setMaxConnectionAge(seconds * 1000);
                break;
            default:
                log.info("Unkown pool mode for this setting: connection age");
        }
        return this;
    }

    public Connection getConnection() throws SQLException {
        Connection retConn = null;
        switch (pool_mode) {
            case BoneCP:
                retConn = bone_cp.getConnection();
                break;
            case C3P0:
                retConn = c3po_ds.getConnection();
                break;

        }
        return retConn;
    }

    public DbmsConnectionPool(LoggingAdapter logger, String cpMode, String driver) throws ClassNotFoundException, PropertyVetoException {
        log = logger;
        pool_mode = getCP(cpMode);
        switch (pool_mode) {
            case BoneCP:
                bonecp_cfg = new BoneCPConfig();
                Class.forName(driver);
                break;
            case C3P0:
                c3po_ds = new ComboPooledDataSource();
                c3po_ds.setDriverClass(driver);
                break;
        }
    }

}
