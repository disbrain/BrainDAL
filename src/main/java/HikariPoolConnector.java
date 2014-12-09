import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.net.DbmsConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class HikariPoolConnector implements DbmsConnectionPool {
    private HikariConfig hikari_cfg = null;
    private HikariDataSource hikari_cp = null;
    private final LoggingAdapter log;

    public LoggingAdapter getLogger() {
        return log;
    }

    public void destroyConnection(Connection destroy_this) {
        hikari_cp.evictConnection(destroy_this);
    }

    public HikariPoolConnector setJdbcUrl(String host, int port, String dbname) {
        Properties source_properties = hikari_cfg.getDataSourceProperties();
        source_properties.setProperty(
                "url",
                String.format("jdbc:mysql://%s:%d/%s", host, port, dbname)
        );
        hikari_cfg.setDataSourceProperties(source_properties);
        return this;
    }

    public HikariPoolConnector setUsername(String username) {
        Properties source_properties = hikari_cfg.getDataSourceProperties();
        source_properties.setProperty("user", username);
        hikari_cfg.setDataSourceProperties(source_properties);
        return this;
    }

    public HikariPoolConnector setPassword(String password) {
        Properties source_properties = hikari_cfg.getDataSourceProperties();
        source_properties.setProperty("password", password);
        hikari_cfg.setDataSourceProperties(source_properties);
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
        log.warning("Minimum connection num not implemented selected pool");
        return this;
    }

    public HikariPoolConnector setMaximumConnectionNum(int limit) {
        hikari_cfg.setMaximumPoolSize(limit);
        return this;
    }

    public HikariPoolConnector setConnectionAcquireIncrement(int increment) {
        log.warning("Connection acquire increment no implemented selected pool");
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
        //hikari_cfg.setDriverClassName(driver);
        log.warning("Driver not implemented with hikariCP");
        return this;
    }

    public HikariPoolConnector setDataSource(String dataSource) {
        hikari_cfg.setDataSourceClassName(dataSource);
        return this;
    }

    public HikariPoolConnector setPrepStmtCacheSize(int cache_size) {
        if (cache_size > 0) {
            Properties prop = hikari_cfg.getDataSourceProperties();
            prop.setProperty("cachePrepStmts", "" + true);
            prop.setProperty("prepStmtCacheSize", "" + cache_size);
            prop.setProperty("prepStmtCacheSqlLimit", "" + 2048);
            prop.setProperty("useServerPrepStmts", "" + true);
            hikari_cfg.setDataSourceProperties(prop);

        }
        return this;
    }

    public HikariPoolConnector(LoggingAdapter logger) {
        log = logger;
        hikari_cfg = new HikariConfig();
    }

}
