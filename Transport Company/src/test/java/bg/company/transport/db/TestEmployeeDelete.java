package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.service.EmployeeService;

import javax.sql.DataSource;
import java.math.BigDecimal;

public class TestEmployeeDelete {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            EmployeeService employeeService = new EmployeeService(new JdbcEmployeeDao(dataSource));
            
            // Specify company ID and employee ID for full control
            Long companyId = 1L;
            Long employeeId = 2L;
            System.out.println("✓ Using company ID: " + companyId);
            System.out.println("✓ Deleting employee ID: " + employeeId);
            
            // Delete the employee
            employeeService.delete(employeeId);
            System.out.println("✓ Employee deleted successfully!");
            
            // Verify deletion
            try {
                employeeService.get(employeeId);
                System.err.println("✗ Error: Employee still exists!");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ Confirmed: Employee does not exist.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
