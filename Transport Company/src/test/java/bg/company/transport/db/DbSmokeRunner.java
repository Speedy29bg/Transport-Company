package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcClientDao;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.Client;
import bg.company.transport.domain.ClientType;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.service.ClientService;
import bg.company.transport.service.CompanyService;
import bg.company.transport.service.EmployeeService;
import bg.company.transport.service.VehicleService;

import javax.sql.DataSource;
import java.math.BigDecimal;

public class DbSmokeRunner {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();

            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));
            ClientService clientService = new ClientService(new JdbcClientDao(dataSource));
            EmployeeService employeeService = new EmployeeService(new JdbcEmployeeDao(dataSource));
            VehicleService vehicleService = new VehicleService(new JdbcVehicleDao(dataSource));

            System.out.println("Starting DB smoke run...");

            TransportCompany company = new TransportCompany("Smoke Run Co", "Sofia, Bulgaria");
            TransportCompany createdCompany = companyService.create(company);
            System.out.println("Created company ID: " + createdCompany.getId());

            Client client = new Client();
            client.setName("Smoke Run Client");
            client.setPhone("+359888000000");
            client.setEmail("smoke.run@example.com");
            client.setType(ClientType.PERSON);
            Client createdClient = clientService.create(client);
            System.out.println("Created client ID: " + createdClient.getId());

            Employee employee = new Employee();
            employee.setCompanyId(createdCompany.getId());
            employee.setFullName("Smoke Run Driver");
            employee.setSalary(BigDecimal.valueOf(2500));
            employee.setRole(EmployeeRole.DRIVER);
            Employee createdEmployee = employeeService.create(employee);
            System.out.println("Created employee ID: " + createdEmployee.getId());

            Vehicle vehicle = new Vehicle();
            vehicle.setCompanyId(createdCompany.getId());
            vehicle.setRegNumber("SMKR-123");
            vehicle.setType(VehicleType.BUS);
            vehicle.setSeatCount(45);
            Vehicle createdVehicle = vehicleService.create(vehicle);
            System.out.println("Created vehicle ID: " + createdVehicle.getId());

            System.out.println("DB smoke run completed successfully. Created records were kept.");
        } catch (Exception e) {
            System.err.println("DB smoke run failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
