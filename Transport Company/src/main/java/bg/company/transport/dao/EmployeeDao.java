package bg.company.transport.dao;

import bg.company.transport.domain.Employee;
import bg.company.transport.domain.DriverQualification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EmployeeDao {
    Employee create(Employee employee);

    Employee update(Employee employee);

    Optional<Employee> findById(Long id);

    List<Employee> list(String qualificationFilter, String sort);

    Set<DriverQualification> findQualifications(Long employeeId);

    void delete(Long id);
}
