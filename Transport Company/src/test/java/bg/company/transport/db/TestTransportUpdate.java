package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Transport;
import bg.company.transport.service.TransportService;

import javax.sql.DataSource;
import java.math.BigDecimal;

public class TestTransportUpdate {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            TransportService transportService = new TransportService(
                new JdbcTransportDao(dataSource),
                new JdbcEmployeeDao(dataSource),
                new JdbcVehicleDao(dataSource)
            );
            
            // Specify transport ID for full control
            Long transportId = 1L;
            System.out.println("✓ Using transport ID: " + transportId);
            
            // Get and update transport
            Transport transport = transportService.get(transportId);
            transport.setPrice(BigDecimal.valueOf(650.00));
            transport.setDestination("Burgas");
            transportService.update(transport);
            
            // Verify changes
            Transport updated = transportService.get(transportId);
            System.out.println("✓ Transport updated successfully!");
            System.out.println("Route: " + updated.getOrigin() + " → " + updated.getDestination());
            System.out.println("New price: " + updated.getPrice() + " BGN");
            System.out.println("Payment status: " + updated.getPaymentStatus());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
