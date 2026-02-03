package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.domain.Employee;
import bg.company.transport.service.EmployeeService;

import javax.sql.DataSource;
import java.util.List;

public class TestEmployeeList {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            EmployeeService employeeService = new EmployeeService(new JdbcEmployeeDao(dataSource));

            List<Employee> employees = employeeService.list(null, "name");
            
            System.out.println("✓ Found " + employees.size() + " employees:");
            System.out.println("═══════════════════════════════════════════════");
            for (Employee emp : employees) {
                System.out.println("ID: " + emp.getId() + " | " + emp.getFullName() + 
                                 " | " + emp.getRole() + " | " + emp.getSalary() + " BGN");
            }
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
