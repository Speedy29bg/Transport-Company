package bg.company.transport.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceFactory {
    private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);
    private static DataSource dataSource;

    private DataSourceFactory() {
    }

    public static DataSource create(Properties properties) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(properties.getProperty("db.url"));
        cfg.setUsername(properties.getProperty("db.user"));
        cfg.setPassword(properties.getProperty("db.password"));
        cfg.setMaximumPoolSize(10);
        cfg.setConnectionTimeout(10_000);
        cfg.setPoolName("transport-hikari-pool");
        log.info("Using database URL {}", cfg.getJdbcUrl());
        return new HikariDataSource(cfg);
    }

    public static DataSource createDataSource() {
        if (dataSource == null) {
            Properties props = ConfigLoader.getProperties();
            dataSource = create(props);
        }
        return dataSource;
    }

    public static DataSource getDataSource() {
        return createDataSource();
    }
}
