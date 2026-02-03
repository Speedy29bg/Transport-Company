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

public class TestVehicleAddBus {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));
            VehicleService vehicleService = new VehicleService(new JdbcVehicleDao(dataSource));

            // Create company for the vehicle
            TransportCompany company = new TransportCompany("Coastal Buses Ltd", "Burgas, Aleksandrovska St 15");
            TransportCompany createdCompany = companyService.create(company);
            System.out.println("✓ Created company ID: " + createdCompany.getId());

            // Create bus
            Vehicle bus = new Vehicle();
            bus.setCompanyId(createdCompany.getId());
            bus.setRegNumber("CA1234BT");
            bus.setType(VehicleType.BUS);
            bus.setSeatCount(52);
            
            Vehicle created = vehicleService.create(bus);
            
            System.out.println("✓ Bus created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Reg. number: " + created.getRegNumber());
            System.out.println("Type: " + created.getType());
            System.out.println("Seat count: " + created.getSeatCount());
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
