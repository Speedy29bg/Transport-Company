package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.service.CompanyService;
import bg.company.transport.service.VehicleService;

import javax.sql.DataSource;

public class TestVehicleAddTruck {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));
            VehicleService vehicleService = new VehicleService(new JdbcVehicleDao(dataSource));

            // Create company for the vehicle
            TransportCompany company = new TransportCompany("Freight Transport Ltd", "Ruse, Pridunavska St 8");
            TransportCompany createdCompany = companyService.create(company);
            System.out.println("✓ Created company ID: " + createdCompany.getId());

            // Create truck
            Vehicle truck = new Vehicle();
            truck.setCompanyId(createdCompany.getId());
            truck.setRegNumber("PC5678TM");
            truck.setType(VehicleType.TRUCK);
            truck.setMaxLoadKg(12000);
            
            Vehicle created = vehicleService.create(truck);
            
            System.out.println("✓ Truck created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Reg. number: " + created.getRegNumber());
            System.out.println("Type: " + created.getType());
            System.out.println("Max load: " + created.getMaxLoadKg() + " kg");
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
