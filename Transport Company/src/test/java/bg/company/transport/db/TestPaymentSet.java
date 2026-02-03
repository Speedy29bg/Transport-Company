package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import bg.company.transport.service.TransportService;

import javax.sql.DataSource;

public class TestPaymentSet {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            TransportService transportService = new TransportService(
                new JdbcTransportDao(dataSource),
                new JdbcEmployeeDao(dataSource),
                new JdbcVehicleDao(dataSource)
            );
            
            // Specify transport ID and payment status for full control
            Long transportId = 1L;
            PaymentStatus newStatus = PaymentStatus.PAID;
            
            System.out.println("✓ Using transport ID: " + transportId);
            System.out.println("✓ Setting payment status to: " + newStatus);
            
            // Update payment status
            transportService.setPaymentStatus(transportId, newStatus);
            
            // Verify changes
            Transport updated = transportService.get(transportId);
            System.out.println("✓ Payment status updated successfully!");
            System.out.println("Route: " + updated.getOrigin() + " → " + updated.getDestination());
            System.out.println("Price: " + updated.getPrice() + " BGN");
            System.out.println("Payment status: " + updated.getPaymentStatus());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
