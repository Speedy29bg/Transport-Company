package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.CargoKind;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import bg.company.transport.service.TransportService;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestTransportAdd {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            TransportService transportService = new TransportService(
                new JdbcTransportDao(dataSource),
                new JdbcEmployeeDao(dataSource),
                new JdbcVehicleDao(dataSource)
            );
            
            // Specify IDs for full control
            Long companyId = 1L;
            Long clientId = 1L;
            Long driverId = 1L;
            Long vehicleId = 1L;
            
            System.out.println("✓ Using company ID: " + companyId);
            System.out.println("✓ Using client ID: " + clientId);
            System.out.println("✓ Using driver ID: " + driverId);
            System.out.println("✓ Using vehicle ID: " + vehicleId);
            
            // Create transport
            Transport transport = new Transport();
            transport.setCompanyId(companyId);
            transport.setClientId(clientId);
            transport.setOrigin("Sofia");
            transport.setDestination("Varna");
            transport.setDepartAt(OffsetDateTime.now().plusDays(1));
            transport.setArriveAt(OffsetDateTime.now().plusDays(1).plusHours(5));
            transport.setCargoKind(CargoKind.PASSENGERS);
            transport.setPassengerCount(25);
            transport.setPrice(BigDecimal.valueOf(500.00));
            transport.setDriverId(driverId);
            transport.setVehicleId(vehicleId);
            transport.setPaymentStatus(PaymentStatus.UNPAID);
            transport.setCreatedAt(OffsetDateTime.now());
            
            Transport created = transportService.create(transport);
            
            System.out.println("✓ Transport created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Route: " + created.getOrigin() + " → " + created.getDestination());
            System.out.println("Passengers: " + created.getPassengerCount());
            System.out.println("Price: " + created.getPrice() + " BGN");
            System.out.println("Payment status: " + created.getPaymentStatus());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
