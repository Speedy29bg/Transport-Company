package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.service.TransportService;

import javax.sql.DataSource;

public class TestTransportDelete {
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
            Long transportId = 2L;
            System.out.println("✓ Deleting transport ID: " + transportId);
            
            // Delete the transport
            transportService.delete(transportId);
            System.out.println("✓ Transport deleted successfully!");
            
            // Verify deletion
            try {
                transportService.get(transportId);
                System.err.println("✗ Error: Transport still exists!");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ Confirmed: Transport does not exist.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
