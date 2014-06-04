import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.net.DbmsConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class HikariPoolConnector implements DbmsConnectionPool {
    private HikariConfig hikari_cfg = null;
    private HikariDataSource hikari_cp = null;
    private final LoggingAdapter log;

    public LoggingAdapter getLogger() {
        return log;
    }

    public HikariPoolConnector setJdbcUrl(String link) {
        hikari_cfg.setJdbcUrl(link);
        return this;
    }

    public HikariPoolConnector setUsername(String username) {
        hikari_cfg.setUsername(username);
        return this;
    }

    public HikariPoolConnector setPassword(String password) {
        hikari_cfg.setPassword(password);
        return this;
    }

    public HikariPoolConnector setStatementsCacheSize(int size) {
        log.warning("Cache size not implemented for HirakiCP");
        return this;
    }

    public HikariPoolConnector setAutoCommit(boolean value) {
        hikari_cfg.setAutoCommit(value);
        return this;
    }

    public HikariPoolConnector setDefaultTransactionIsolation(String isolation_level) {
        hikari_cfg.setTransactionIsolation(isolation_level);
        return this;
    }

    public HikariPoolConnector setMinimumConnectionNum(int limit) {
        hikari_cfg.setMinimumPoolSize(limit);
        return this;
    }

    public HikariPoolConnector setMaximumConnectionNum(int limit) {
        hikari_cfg.setMaximumPoolSize(limit);
        return this;
    }

    public HikariPoolConnector setConnectionAcquireIncrement(int increment) {
        hikari_cfg.setAcquireIncrement(increment);
        return this;
    }

    public HikariPoolConnector setPartitionsNum(int count) {
        log.warning("Partitions logic not implemented selected pool");
        return this;
    }

    public HikariPoolConnector setIdleConnectionTestPeriod(int period) {
        log.warning("Feature Idle Connection Test Period not implemented for HikariCP");
        return this;
    }

    public HikariPoolConnector setStatistics(boolean value) {
        log.info("Statistics not implemented for C3P0");
        return this;
    }

    public HikariPoolConnector setPoolAvailabilityThreshold(int threshold) {
        log.info("Threshold not implemented with Hikari");
        return this;
    }

    public HikariPoolConnector setConnectionLeakWatch(long msec_timeout) {
        if (msec_timeout > 0)
            hikari_cfg.setLeakDetectionThreshold(msec_timeout);
        return this;
    }

    public String getStatistics() {
        log.info("Statistics not implemented fore Hikari");
        return null;
    }

    public HikariPoolConnector createPool() {
        hikari_cp = new HikariDataSource(hikari_cfg);
        return this;
    }

    public HikariPoolConnector setMaxConnectionAge(int seconds) {
        if (seconds > 0)
            hikari_cfg.setIdleTimeout(seconds * 1000);
        return this;
    }

    public Connection getConnection() throws SQLException {
        return hikari_cp.getConnection();
    }

    public HikariPoolConnector setLogLevel(String loglevel) {
        log.warning("Logging level feature not implemented for Hikari\n");
        return this;
    }

    public HikariPoolConnector setInitPolitic(boolean lazyness) {
        log.warning("Init politic not supported for Hikari");
        return this;
    }

    public HikariPoolConnector setConnectionTimeout(long value_in_seconds) {
        if (value_in_seconds > 0)
            hikari_cfg.setConnectionTimeout(value_in_seconds * 1000);
        return this;
    }

    public HikariPoolConnector setDriver(String driver) {
        hikari_cfg.setDriverClassName(driver);
        return this;
    }

    public HikariPoolConnector(LoggingAdapter logger) {
        log = logger;
        hikari_cfg = new HikariConfig();
    }

}
