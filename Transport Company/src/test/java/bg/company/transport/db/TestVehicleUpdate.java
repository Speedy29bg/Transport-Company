package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.service.VehicleService;

import javax.sql.DataSource;

public class TestVehicleUpdate {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            VehicleService vehicleService = new VehicleService(new JdbcVehicleDao(dataSource));
            
            // Specify company ID and vehicle ID for full control
            Long companyId = 1L;
            Long vehicleId = 1L;
            System.out.println("✓ Using company ID: " + companyId);
            System.out.println("✓ Using vehicle ID: " + vehicleId);
            
            // Get and update vehicle capacity
            Vehicle vehicle = vehicleService.get(vehicleId);
            vehicle.setCompanyId(companyId);
            vehicle.setMaxLoadKg(2000);
            vehicleService.update(vehicle);
            
            // Verify changes
            Vehicle updated = vehicleService.get(vehicleId);
            System.out.println("✓ Vehicle updated successfully!");
            System.out.println("Company ID: " + updated.getCompanyId());
            System.out.println("Reg. number: " + updated.getRegNumber());
            System.out.println("New max load: " + updated.getMaxLoadKg() + " kg");
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
