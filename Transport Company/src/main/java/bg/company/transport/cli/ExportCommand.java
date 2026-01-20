package bg.company.transport.cli;

import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Transport;
import bg.company.transport.io.TransportCsvExporter;
import bg.company.transport.service.TransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileWriter;
import java.util.List;

@Command(name = "export", description = "Export data")
public class ExportCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ExportCommand.class);
    private final TransportService transportService;
    private final TransportCsvExporter exporter = new TransportCsvExporter();

    public ExportCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var transportDao = new JdbcTransportDao(dataSource);
            var employeeDao = new JdbcEmployeeDao(dataSource);
            var vehicleDao = new JdbcVehicleDao(dataSource);
            this.transportService = new TransportService(transportDao, employeeDao, vehicleDao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ExportCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Export data. Use: export transports --out=file.csv");
    }

    @Command(name = "transports", description = "Export transports to CSV")
    public void transports(
            @Option(names = {"--out"}, description = "Output file path", required = true) String outputFile,
            @Option(names = {"--destination"}, description = "Filter by destination") String destination) {
        try {
            List<Transport> transports = transportService.list(destination, "depart");
            try (FileWriter writer = new FileWriter(outputFile)) {
                exporter.export(transports, writer);
            }
            System.out.println("Exported " + transports.size() + " transports to " + outputFile);
        } catch (Exception e) {
            System.err.println("Error exporting transports: " + e.getMessage());
            logger.error("Failed to export transports", e);
        }
    }
}
