package bg.company.transport.validation;

import bg.company.transport.domain.DriverQualification;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeValidatorTest {

    private final EmployeeValidator validator = new EmployeeValidator();

    @Test
    void nonDriverCannotHaveQualifications() {
        Employee employee = new Employee();
        employee.setCompanyId(1L);
        employee.setFullName("Admin User");
        employee.setSalary(BigDecimal.valueOf(3000));
        employee.setRole(EmployeeRole.ADMIN);
        employee.setQualifications(EnumSet.of(DriverQualification.PASSENGERS_12_PLUS));

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(employee));
        assertEquals("Only drivers can have qualifications", ex.getFieldErrors().get("qualifications"));
    }

    @Test
    void requiresPositiveSalary() {
        Employee employee = new Employee();
        employee.setCompanyId(1L);
        employee.setFullName("No Salary");
        employee.setRole(EmployeeRole.DRIVER);
        employee.setSalary(BigDecimal.ZERO);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(employee));
        assertEquals("Salary must be positive", ex.getFieldErrors().get("salary"));
    }
}
