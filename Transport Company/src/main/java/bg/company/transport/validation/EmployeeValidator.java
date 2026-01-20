package bg.company.transport.validation;

import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.exception.ValidationException;

import java.math.BigDecimal;

public class EmployeeValidator {
    public void validate(Employee employee) {
        ValidationResult result = new ValidationResult();
        
        // Basic field validation
        if (employee.getCompanyId() == null) {
            result.addError("companyId", "Company is required");
        }
        if (employee.getFullName() == null || employee.getFullName().isBlank()) {
            result.addError("fullName", "Full name is required");
        }
        
        // Salary validation
        BigDecimal salary = employee.getSalary();
        if (salary == null || salary.signum() <= 0) {
            result.addError("salary", "Salary must be positive");
        }
        
        // Role validation
        EmployeeRole role = employee.getRole();
        if (role == null) {
            result.addError("role", "Role is required");
        }
        
        // Qualifications validation: Only drivers can have qualifications
        if (role != null && role != EmployeeRole.DRIVER) {
            if (employee.getQualifications() != null && !employee.getQualifications().isEmpty()) {
                result.addError("qualifications", "Only drivers can have qualifications");
            }
        }
        
        if (!result.isValid()) {
            throw new ValidationException("Employee validation failed", result.getFieldErrors());
        }
    }
}
