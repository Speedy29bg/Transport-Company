package bg.company.transport.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class FlywayMigrator {
    private static final Logger log = LoggerFactory.getLogger(FlywayMigrator.class);

    private FlywayMigrator() {
    }

    public static void migrate(DataSource dataSource, Properties properties) {
        String schemas = properties.getProperty("flyway.schemas", "transport_company");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemas)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        log.info("Flyway migrations applied for schemas {}", schemas);
    }

    public static void migrate() {
        try {
            DataSource dataSource = DataSourceFactory.createDataSource();
            Properties properties = ConfigLoader.getProperties();
            migrate(dataSource, properties);
        } catch (Exception e) {
            log.error("Flyway migration failed", e);
            throw new RuntimeException("Database migration failed", e);
        }
    }
}
