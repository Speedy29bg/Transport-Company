package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.domain.DriverQualification;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.service.CompanyService;
import bg.company.transport.service.EmployeeService;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.EnumSet;

public class TestEmployeeAddDriver {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));
            EmployeeService employeeService = new EmployeeService(new JdbcEmployeeDao(dataSource));

            // Create company for the employee
            TransportCompany company = new TransportCompany("Bulgaria Transport Ltd", "Plovdiv, Maritsa Blvd 20");
            TransportCompany createdCompany = companyService.create(company);
            System.out.println("✓ Created company ID: " + createdCompany.getId());

            // Create driver
            Employee driver = new Employee();
            driver.setCompanyId(createdCompany.getId());
            driver.setFullName("George Anderson");
            driver.setSalary(BigDecimal.valueOf(3500));
            driver.setRole(EmployeeRole.DRIVER);
            driver.setQualifications(EnumSet.of(
                DriverQualification.PASSENGERS_12_PLUS,
                DriverQualification.HAZMAT
            ));
            
            Employee created = employeeService.create(driver);
            
            System.out.println("✓ Driver created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getFullName());
            System.out.println("Salary: " + created.getSalary() + " BGN");
            System.out.println("Role: " + created.getRole());
            System.out.println("Qualifications: " + created.getQualifications());
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
