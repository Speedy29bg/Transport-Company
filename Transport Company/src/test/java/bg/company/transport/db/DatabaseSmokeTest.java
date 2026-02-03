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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseSmokeTest {

    private static DataSource dataSource;
    private static CompanyService companyService;
    private static ClientService clientService;
    private static EmployeeService employeeService;
    private static VehicleService vehicleService;

    @BeforeAll
    static void setUp() {
        ConfigLoader.loadDatabaseProperties();
        dataSource = DataSourceFactory.createDataSource();

        Assumptions.assumeTrue(canConnect(dataSource), "Database not reachable; skipping DB smoke test");

        companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));
        clientService = new ClientService(new JdbcClientDao(dataSource));
        employeeService = new EmployeeService(new JdbcEmployeeDao(dataSource));
        vehicleService = new VehicleService(new JdbcVehicleDao(dataSource));
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (dataSource instanceof AutoCloseable closeable) {
            closeable.close();
        }
    }

    @Test
    void crudSmokeTest() {
        // Company
        TransportCompany company = new TransportCompany("Smoke Test Co", "Sofia, Bulgaria");
        TransportCompany createdCompany = companyService.create(company);
        assertNotNull(createdCompany.getId());

        TransportCompany fetchedCompany = companyService.get(createdCompany.getId());
        assertEquals("Smoke Test Co", fetchedCompany.getName());

        fetchedCompany.setName("Smoke Test Co Updated");
        companyService.update(fetchedCompany);
        assertEquals("Smoke Test Co Updated", companyService.get(createdCompany.getId()).getName());

        // Client
        Client client = new Client();
        client.setName("Smoke Client");
        client.setPhone("+359888000000");
        client.setEmail("smoke.client@example.com");
        client.setType(ClientType.PERSON);
        Client createdClient = clientService.create(client);
        assertNotNull(createdClient.getId());

        Client fetchedClient = clientService.get(createdClient.getId());
        assertEquals("Smoke Client", fetchedClient.getName());

        // Employee
        Employee employee = new Employee();
        employee.setCompanyId(createdCompany.getId());
        employee.setFullName("Smoke Driver");
        employee.setSalary(BigDecimal.valueOf(2500));
        employee.setRole(EmployeeRole.DRIVER);
        Employee createdEmployee = employeeService.create(employee);
        assertNotNull(createdEmployee.getId());

        Employee fetchedEmployee = employeeService.get(createdEmployee.getId());
        assertEquals("Smoke Driver", fetchedEmployee.getFullName());

        // Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setCompanyId(createdCompany.getId());
        vehicle.setRegNumber("SMK-123");
        vehicle.setType(VehicleType.BUS);
        vehicle.setSeatCount(45);
        Vehicle createdVehicle = vehicleService.create(vehicle);
        assertNotNull(createdVehicle.getId());

        Vehicle fetchedVehicle = vehicleService.get(createdVehicle.getId());
        assertEquals("SMK-123", fetchedVehicle.getRegNumber());

        // Cleanup (delete in reverse order)
        vehicleService.delete(createdVehicle.getId());
        employeeService.delete(createdEmployee.getId());
        clientService.delete(createdClient.getId());
        companyService.delete(createdCompany.getId());

        assertThrows(IllegalArgumentException.class, () -> companyService.get(createdCompany.getId()));
    }

    private static boolean canConnect(DataSource ds) {
        try (Connection con = ds.getConnection()) {
            return con.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
