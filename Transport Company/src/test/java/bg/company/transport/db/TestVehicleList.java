package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.service.VehicleService;

import javax.sql.DataSource;
import java.util.List;

public class TestVehicleList {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            VehicleService vehicleService = new VehicleService(new JdbcVehicleDao(dataSource));

            // Get first available company from DB (ID = 1 usually exists)
            Long companyId = 1L;
            List<Vehicle> vehicles = vehicleService.list(companyId);
            
            System.out.println("✓ Found " + vehicles.size() + " vehicles for company " + companyId + ":");
            System.out.println("═══════════════════════════════════════════════");
            for (Vehicle vehicle : vehicles) {
                System.out.println("ID: " + vehicle.getId() + " | " + vehicle.getRegNumber() + 
                                 " | " + vehicle.getType());
            }
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
