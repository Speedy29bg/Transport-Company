package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.service.VehicleService;

import javax.sql.DataSource;

public class TestVehicleDelete {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            VehicleService vehicleService = new VehicleService(new JdbcVehicleDao(dataSource));
            
            // Specify company ID and vehicle ID for full control
            Long companyId = 1L;
            Long vehicleId = 2L;
            System.out.println("✓ Using company ID: " + companyId);
            System.out.println("✓ Deleting vehicle ID: " + vehicleId);
            
            // Delete the vehicle
            vehicleService.delete(vehicleId);
            System.out.println("✓ Vehicle deleted successfully!");
            
            // Verify deletion
            try {
                vehicleService.get(vehicleId);
                System.err.println("✗ Error: Vehicle still exists!");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ Confirmed: Vehicle does not exist.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
