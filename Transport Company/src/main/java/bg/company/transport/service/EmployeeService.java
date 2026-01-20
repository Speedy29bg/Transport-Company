package bg.company.transport.service;

import bg.company.transport.dao.EmployeeDao;
import bg.company.transport.domain.DriverQualification;
import bg.company.transport.domain.Employee;
import bg.company.transport.exception.ValidationException;
import bg.company.transport.validation.EmployeeValidator;
import bg.company.transport.validation.ValidationResult;

import java.util.List;
import java.util.Set;

public class EmployeeService {
    private final EmployeeDao employeeDao;
    private final EmployeeValidator validator = new EmployeeValidator();

    public EmployeeService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    public Employee create(Employee employee) {
        validator.validate(employee);
        return employeeDao.create(employee);
    }

    public Employee update(Employee employee) {
        validator.validate(employee);
        return employeeDao.update(employee);
    }

    public Employee get(Long id) {
        return employeeDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    public List<Employee> list(String qualificationFilter, String sort) {
        return employeeDao.list(qualificationFilter, sort);
    }

    public Set<DriverQualification> qualifications(Long employeeId) {
        return employeeDao.findQualifications(employeeId);
    }

    public void delete(Long id) {
        employeeDao.delete(id);
    }
}
