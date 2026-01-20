package bg.company.transport.cli;

import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "vehicle", description = "Manage vehicles")
public class VehicleCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(VehicleCommand.class);
    private final VehicleService vehicleService;

    public VehicleCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var dao = new JdbcVehicleDao(dataSource);
            this.vehicleService = new VehicleService(dao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize VehicleCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Vehicle management. Use: vehicle add|edit|delete|get|list");
    }

    @Command(name = "add", description = "Add a new vehicle")
    public void add(
            @Parameters(index = "0", description = "Company ID") Long companyId,
            @Parameters(index = "1", description = "Registration number") String regNumber,
            @Parameters(index = "2", description = "Type: BUS|TRUCK|TANKER|VAN") String type,
            @Option(names = {"--seats"}, description = "Seat count (for BUS)") Integer seatCount,
            @Option(names = {"--load"}, description = "Max load in kg (for TRUCK)") Integer maxLoadKg,
            @Option(names = {"--volume"}, description = "Volume in liters (for TANKER)") Integer volumeLiters) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setCompanyId(companyId);
            vehicle.setRegNumber(regNumber);
            vehicle.setType(VehicleType.valueOf(type.toUpperCase()));
            vehicle.setSeatCount(seatCount);
            vehicle.setMaxLoadKg(maxLoadKg);
            vehicle.setVolumeLiters(volumeLiters);
            
            Vehicle created = vehicleService.create(vehicle);
            System.out.println("Vehicle created successfully with ID: " + created.getId());
        } catch (Exception e) {
            System.err.println("Error creating vehicle: " + e.getMessage());
            logger.error("Failed to create vehicle", e);
        }
    }

    @Command(name = "edit", description = "Edit a vehicle")
    public void edit(
            @Parameters(index = "0", description = "Vehicle ID") Long id,
            @Option(names = {"-c", "--companyId"}, description = "Company ID") Long companyId,
            @Option(names = {"-r", "--regNumber"}, description = "Registration number") String regNumber,
            @Option(names = {"-t", "--type"}, description = "Type") String type,
            @Option(names = {"--seats"}, description = "Seat count") Integer seatCount,
            @Option(names = {"--load"}, description = "Max load in kg") Integer maxLoadKg,
            @Option(names = {"--volume"}, description = "Volume in liters") Integer volumeLiters) {
        try {
            Vehicle vehicle = vehicleService.get(id);
            if (companyId != null) vehicle.setCompanyId(companyId);
            if (regNumber != null) vehicle.setRegNumber(regNumber);
            if (type != null) vehicle.setType(VehicleType.valueOf(type.toUpperCase()));
            if (seatCount != null) vehicle.setSeatCount(seatCount);
            if (maxLoadKg != null) vehicle.setMaxLoadKg(maxLoadKg);
            if (volumeLiters != null) vehicle.setVolumeLiters(volumeLiters);
            
            vehicleService.update(vehicle);
            System.out.println("Vehicle updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            logger.error("Failed to update vehicle", e);
        }
    }

    @Command(name = "get", description = "Get vehicle by ID")
    public void get(@Parameters(index = "0", description = "Vehicle ID") Long id) {
        try {
            Vehicle vehicle = vehicleService.get(id);
            System.out.println(vehicle);
        } catch (Exception e) {
            System.err.println("Error fetching vehicle: " + e.getMessage());
            logger.error("Failed to fetch vehicle", e);
        }
    }

    @Command(name = "list", description = "List all vehicles for a company")
    public void list(@Parameters(index = "0", description = "Company ID") Long companyId) {
        try {
            List<Vehicle> vehicles = vehicleService.list(companyId);
            if (vehicles.isEmpty()) {
                System.out.println("No vehicles found");
            } else {
                System.out.println("\n=== VEHICLES ===");
                vehicles.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Error listing vehicles: " + e.getMessage());
            logger.error("Failed to list vehicles", e);
        }
    }

    @Command(name = "delete", description = "Delete a vehicle")
    public void delete(@Parameters(index = "0", description = "Vehicle ID") Long id) {
        try {
            vehicleService.delete(id);
            System.out.println("Vehicle deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting vehicle: " + e.getMessage());
            logger.error("Failed to delete vehicle", e);
        }
    }
}
