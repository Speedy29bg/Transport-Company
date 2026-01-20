package bg.company.transport.cli;

import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.io.ImportResult;
import bg.company.transport.io.TransportCsvImporter;
import bg.company.transport.service.TransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileReader;

@Command(name = "import", description = "Import data")
public class ImportCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ImportCommand.class);
    private final TransportService transportService;
    private final TransportCsvImporter importer = new TransportCsvImporter();

    public ImportCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var transportDao = new JdbcTransportDao(dataSource);
            var employeeDao = new JdbcEmployeeDao(dataSource);
            var vehicleDao = new JdbcVehicleDao(dataSource);
            this.transportService = new TransportService(transportDao, employeeDao, vehicleDao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ImportCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Import data. Use: import transports --in=file.csv");
    }

    @Command(name = "transports", description = "Import transports from CSV")
    public void transports(
            @Option(names = {"--in"}, description = "Input file path", required = true) String inputFile) {
        try {
            try (FileReader reader = new FileReader(inputFile)) {
                ImportResult result = importer.importFile(reader, transportService);
                System.out.println("Import completed:");
                System.out.println("  Imported: " + result.imported());
                System.out.println("  Failed: " + result.failed());
            }
        } catch (Exception e) {
            System.err.println("Error importing transports: " + e.getMessage());
            logger.error("Failed to import transports", e);
        }
    }
}
