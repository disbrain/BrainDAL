import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.net.DbmsConnectionPool;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.Statistics;

import java.sql.Connection;


public class BoneCPConnector implements DbmsConnectionPool {

    private BoneCPConfig bonecp_cfg = null;
    private volatile BoneCP bone_cp = null;
    private final LoggingAdapter log;

    public LoggingAdapter getLogger() {
        return log;
    }

    public BoneCPConnector setJdbcUrl(String link) {
        bonecp_cfg.setJdbcUrl(link);
        return this;
    }

    public BoneCPConnector setUsername(String username) {
        bonecp_cfg.setUsername(username);
        return this;
    }

    public BoneCPConnector setPassword(String password) {
        bonecp_cfg.setPassword(password);
        return this;
    }

    public BoneCPConnector setStatementsCacheSize(int size) {
        bonecp_cfg.setStatementsCacheSize(size);
        return this;
    }

    public BoneCPConnector setAutoCommit(boolean value) {
        bonecp_cfg.setDefaultAutoCommit(value);
        return this;
    }

    public BoneCPConnector setDefaultTransactionIsolation(String isolation_level) {
        bonecp_cfg.setDefaultTransactionIsolation(isolation_level);
        return this;
    }

    public BoneCPConnector setMinimumConnectionNum(int limit) {
        bonecp_cfg.setMinConnectionsPerPartition(limit);
        return this;
    }

    public BoneCPConnector setMaximumConnectionNum(int limit) {
        bonecp_cfg.setMaxConnectionsPerPartition(limit);
        return this;
    }

    public BoneCPConnector setConnectionAcquireIncrement(int increment) {
        bonecp_cfg.setAcquireIncrement(increment);
        return this;
    }

    public BoneCPConnector setPartitionsNum(int count) {
        bonecp_cfg.setPartitionCount(count);
        return this;
    }

    public BoneCPConnector setIdleConnectionTestPeriod(int period) {
        bonecp_cfg.setIdleConnectionTestPeriod(period);
        return this;
    }

    public BoneCPConnector setStatistics(boolean value) {
        bonecp_cfg.setStatisticsEnabled(value);
        return this;
    }

    public BoneCPConnector setPoolAvailabilityThreshold(int threshold) {
        bonecp_cfg.setPoolAvailabilityThreshold(threshold);
        return this;
    }

    public BoneCPConnector setConnectionLeakWatch(long msec_timeout) {
        log.error("Connection leak watch timeout not implemented for BONECP");
        return this;
    }

    public String getStatistics() {
        Statistics stats = bone_cp.getStatistics();
        return String.format("CacheHitRatio: %f CacheHit: %d CacheMiss: %d\nConnectionsRequested: %d ConnectionWaitTimeAvg: %f\nStatementsCached: %d StatementsPrepared: %d\nCreatedConnections: %d TotalFree: %d TotalLeased: %d",
                stats.getCacheHitRatio(), stats.getCacheHits(), stats.getCacheMiss(), stats.getConnectionsRequested(), stats.getConnectionWaitTimeAvg(), stats.getStatementsCached(), stats.getStatementsPrepared(), stats.getTotalCreatedConnections(), stats.getTotalFree(), stats.getTotalLeased());
    }

    public BoneCPConnector createPool() throws Exception {
        bone_cp = new BoneCP(bonecp_cfg);
        return this;
    }

    public BoneCPConnector setMaxConnectionAge(int seconds) {
        if (seconds > 0)
            bonecp_cfg.setMaxConnectionAgeInSeconds(seconds);
        return this;
    }

    public Connection getConnection() throws Exception {
        return bone_cp.getConnection();
    }

    public BoneCPConnector setLogLevel(String loglevel) {
        log.warning("Logging level feature not implemented for BONECP\n");
        return this;
    }

    public BoneCPConnector setInitPolitic(boolean lazyness) {
        bonecp_cfg.setLazyInit(lazyness);

        return this;
    }

    public BoneCPConnector setConnectionTimeout(long value_in_seconds) {
        log.warning("Connection timeout not implemented for BONECP");
        return this;
    }

    public BoneCPConnector setDriver(String driver) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            log.error(e.getStackTrace().toString());
        }
        return this;
    }

    public BoneCPConnector(LoggingAdapter logger) {
        log = logger;
        bonecp_cfg = new BoneCPConfig();
    }

}
