package bg.company.transport.domain;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class Employee {
    private Long id;
    private Long companyId;
    private String fullName;
    private BigDecimal salary;
    private EmployeeRole role;
    private Set<DriverQualification> qualifications = EnumSet.noneOf(DriverQualification.class);

    public Employee() {
    }

    public Employee(Long id, Long companyId, String fullName, BigDecimal salary, EmployeeRole role, Set<DriverQualification> qualifications) {
        this.id = id;
        this.companyId = companyId;
        this.fullName = fullName;
        this.salary = salary;
        this.role = role;
        if (qualifications != null) {
            this.qualifications = EnumSet.copyOf(qualifications);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    public Set<DriverQualification> getQualifications() {
        return qualifications;
    }

    public void setQualifications(Set<DriverQualification> qualifications) {
        if (qualifications == null) {
            this.qualifications = EnumSet.noneOf(DriverQualification.class);
        } else {
            this.qualifications = EnumSet.copyOf(qualifications);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", companyId=" + companyId +
                ", fullName='" + fullName + '\'' +
                ", salary=" + salary +
                ", role=" + role +
                ", qualifications=" + qualifications +
                '}';
    }
}
