package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.service.EmployeeService;

import javax.sql.DataSource;
import java.math.BigDecimal;

public class TestEmployeeUpdate {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            EmployeeService employeeService = new EmployeeService(new JdbcEmployeeDao(dataSource));
            
            // Specify company ID and employee ID for full control
            Long companyId = 1L;
            Long employeeId = 1L;
            System.out.println("✓ Using company ID: " + companyId);
            System.out.println("✓ Using employee ID: " + employeeId);
            
            // Get and update employee
            Employee employee = employeeService.get(employeeId);
            employee.setCompanyId(companyId);
            employee.setSalary(BigDecimal.valueOf(3200));
            employee.setRole(EmployeeRole.ADMIN);
            employeeService.update(employee);
            
            // Verify changes
            Employee updated = employeeService.get(employeeId);
            System.out.println("✓ Employee updated successfully!");
            System.out.println("Company ID: " + updated.getCompanyId());
            System.out.println("New salary: " + updated.getSalary() + " BGN");
            System.out.println("New role: " + updated.getRole());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
