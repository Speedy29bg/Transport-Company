package bg.company.transport.cli;

import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.CargoKind;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import bg.company.transport.service.TransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Command(name = "transport", description = "Manage transports")
public class TransportSubCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TransportSubCommand.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final TransportService transportService;

    public TransportSubCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var transportDao = new JdbcTransportDao(dataSource);
            var employeeDao = new JdbcEmployeeDao(dataSource);
            var vehicleDao = new JdbcVehicleDao(dataSource);
            this.transportService = new TransportService(transportDao, employeeDao, vehicleDao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TransportSubCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Transport management. Use: transport add|edit|delete|get|list");
    }

    @Command(name = "add", description = "Add a new transport")
    public void add(
            @Parameters(index = "0", description = "Company ID") Long companyId,
            @Parameters(index = "1", description = "Client ID") Long clientId,
            @Parameters(index = "2", description = "Origin") String origin,
            @Parameters(index = "3", description = "Destination") String destination,
            @Parameters(index = "4", description = "Depart at (ISO format)") String departAt,
            @Parameters(index = "5", description = "Arrive at (ISO format)") String arriveAt,
            @Parameters(index = "6", description = "Cargo kind: GOODS|PASSENGERS") String cargoKind,
            @Parameters(index = "7", description = "Price") BigDecimal price,
            @Parameters(index = "8", description = "Driver ID") Long driverId,
            @Parameters(index = "9", description = "Vehicle ID") Long vehicleId,
            @Option(names = {"--weight"}, description = "Cargo weight in kg (for GOODS)") Integer cargoWeightKg,
            @Option(names = {"--passengers"}, description = "Passenger count (for PASSENGERS)") Integer passengerCount,
            @Option(names = {"--status"}, description = "Payment status: PAID|UNPAID", defaultValue = "UNPAID") String paymentStatus) {
        try {
            Transport transport = new Transport();
            transport.setCompanyId(companyId);
            transport.setClientId(clientId);
            transport.setOrigin(origin);
            transport.setDestination(destination);
            transport.setDepartAt(OffsetDateTime.parse(departAt, FORMATTER));
            transport.setArriveAt(OffsetDateTime.parse(arriveAt, FORMATTER));
            transport.setCargoKind(CargoKind.valueOf(cargoKind.toUpperCase()));
            transport.setPrice(price);
            transport.setDriverId(driverId);
            transport.setVehicleId(vehicleId);
            transport.setCargoWeightKg(cargoWeightKg);
            transport.setPassengerCount(passengerCount);
            transport.setPaymentStatus(PaymentStatus.valueOf(paymentStatus.toUpperCase()));
            
            Transport created = transportService.create(transport);
            System.out.println("Transport created successfully with ID: " + created.getId());
        } catch (Exception e) {
            System.err.println("Error creating transport: " + e.getMessage());
            logger.error("Failed to create transport", e);
        }
    }

    @Command(name = "get", description = "Get transport by ID")
    public void get(@Parameters(index = "0", description = "Transport ID") Long id) {
        try {
            Transport transport = transportService.get(id);
            System.out.println(transport);
        } catch (Exception e) {
            System.err.println("Error fetching transport: " + e.getMessage());
            logger.error("Failed to fetch transport", e);
        }
    }

    @Command(name = "list", description = "List all transports")
    public void list(
            @Option(names = {"--destination"}, description = "Filter by destination") String destination,
            @Option(names = {"--sort"}, description = "Sort by: destination|depart", defaultValue = "depart") String sort) {
        try {
            List<Transport> transports = transportService.list(destination, sort);
            if (transports.isEmpty()) {
                System.out.println("No transports found");
            } else {
                System.out.println("\n=== TRANSPORTS ===");
                transports.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Error listing transports: " + e.getMessage());
            logger.error("Failed to list transports", e);
        }
    }

    @Command(name = "delete", description = "Delete a transport")
    public void delete(@Parameters(index = "0", description = "Transport ID") Long id) {
        try {
            transportService.delete(id);
            System.out.println("Transport deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting transport: " + e.getMessage());
            logger.error("Failed to delete transport", e);
        }
    }
}
