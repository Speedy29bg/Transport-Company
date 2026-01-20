package bg.company.transport.cli;

import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.domain.DriverQualification;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Command(name = "employee", description = "Manage employees")
public class EmployeeCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeCommand.class);
    private final EmployeeService employeeService;

    public EmployeeCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var dao = new JdbcEmployeeDao(dataSource);
            this.employeeService = new EmployeeService(dao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EmployeeCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Employee management. Use: employee add|edit|delete|get|list");
    }

    @Command(name = "add", description = "Add a new employee")
    public void add(
            @Parameters(index = "0", description = "Company ID") Long companyId,
            @Parameters(index = "1", description = "Full name") String fullName,
            @Parameters(index = "2", description = "Salary") BigDecimal salary,
            @Option(names = {"-r", "--role"}, description = "Role: DRIVER|DISPATCHER|ADMIN", defaultValue = "DRIVER") String role,
            @Option(names = {"-q", "--qualifications"}, description = "Driver qualifications (comma-separated): HAZMAT,PASSENGERS_12_PLUS,OVERSIZED", split = ",") String[] qualifications) {
        try {
            Employee employee = new Employee();
            employee.setCompanyId(companyId);
            employee.setFullName(fullName);
            employee.setSalary(salary);
            employee.setRole(EmployeeRole.valueOf(role.toUpperCase()));
            
            if (qualifications != null && qualifications.length > 0) {
                Set<DriverQualification> quals = EnumSet.noneOf(DriverQualification.class);
                for (String q : qualifications) {
                    quals.add(DriverQualification.valueOf(q.trim().toUpperCase()));
                }
                employee.setQualifications(quals);
            }
            
            Employee created = employeeService.create(employee);
            System.out.println("Employee created successfully with ID: " + created.getId());
        } catch (Exception e) {
            System.err.println("Error creating employee: " + e.getMessage());
            logger.error("Failed to create employee", e);
        }
    }

    @Command(name = "edit", description = "Edit an employee")
    public void edit(
            @Parameters(index = "0", description = "Employee ID") Long id,
            @Option(names = {"-c", "--companyId"}, description = "Company ID") Long companyId,
            @Option(names = {"-n", "--name"}, description = "Full name") String fullName,
            @Option(names = {"-s", "--salary"}, description = "Salary") BigDecimal salary,
            @Option(names = {"-r", "--role"}, description = "Role") String role,
            @Option(names = {"-q", "--qualifications"}, description = "Qualifications", split = ",") String[] qualifications) {
        try {
            Employee employee = employeeService.get(id);
            if (companyId != null) employee.setCompanyId(companyId);
            if (fullName != null) employee.setFullName(fullName);
            if (salary != null) employee.setSalary(salary);
            if (role != null) employee.setRole(EmployeeRole.valueOf(role.toUpperCase()));
            if (qualifications != null) {
                Set<DriverQualification> quals = EnumSet.noneOf(DriverQualification.class);
                for (String q : qualifications) {
                    quals.add(DriverQualification.valueOf(q.trim().toUpperCase()));
                }
                employee.setQualifications(quals);
            }
            employeeService.update(employee);
            System.out.println("Employee updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            logger.error("Failed to update employee", e);
        }
    }

    @Command(name = "get", description = "Get employee by ID")
    public void get(@Parameters(index = "0", description = "Employee ID") Long id) {
        try {
            Employee employee = employeeService.get(id);
            System.out.println(employee);
        } catch (Exception e) {
            System.err.println("Error fetching employee: " + e.getMessage());
            logger.error("Failed to fetch employee", e);
        }
    }

    @Command(name = "list", description = "List all employees")
    public void list(
            @Option(names = {"--qualification"}, description = "Filter by qualification") String qualification,
            @Option(names = {"--sort"}, description = "Sort by: salary|name", defaultValue = "name") String sort) {
        try {
            List<Employee> employees = employeeService.list(qualification, sort);
            if (employees.isEmpty()) {
                System.out.println("No employees found");
            } else {
                System.out.println("\n=== EMPLOYEES ===");
                employees.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Error listing employees: " + e.getMessage());
            logger.error("Failed to list employees", e);
        }
    }

    @Command(name = "delete", description = "Delete an employee")
    public void delete(@Parameters(index = "0", description = "Employee ID") Long id) {
        try {
            employeeService.delete(id);
            System.out.println("Employee deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            logger.error("Failed to delete employee", e);
        }
    }
}
