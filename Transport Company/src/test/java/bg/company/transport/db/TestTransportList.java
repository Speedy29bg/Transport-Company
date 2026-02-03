package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Transport;
import bg.company.transport.service.TransportService;

import javax.sql.DataSource;
import java.util.List;

public class TestTransportList {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            TransportService transportService = new TransportService(
                new JdbcTransportDao(dataSource),
                new JdbcEmployeeDao(dataSource),
                new JdbcVehicleDao(dataSource)
            );
            
            // List all transports
            List<Transport> transports = transportService.list(null, "depart");
            
            System.out.println("✓ Found " + transports.size() + " transports:");
            System.out.println("═══════════════════════════════════════════════════");
            for (Transport t : transports) {
                System.out.println("ID: " + t.getId() + " | " + 
                                 t.getOrigin() + " → " + t.getDestination() + " | " +
                                 t.getPrice() + " BGN | " + t.getPaymentStatus());
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
