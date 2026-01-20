package bg.company.transport;

import bg.company.transport.cli.TransportCommand;
import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.FlywayMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting Transport Company Management Application");
            
            // Initialize database
            ConfigLoader.loadDatabaseProperties();
            FlywayMigrator.migrate();
            
            logger.debug("Database initialized successfully");
            
            // Start CLI
            int exitCode = new CommandLine(new TransportCommand()).execute(args);
            System.exit(exitCode);
        } catch (Exception e) {
            logger.error("Application startup failed", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
