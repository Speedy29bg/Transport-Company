package bg.company.transport.dao;

import bg.company.transport.domain.DriverQualification;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.exception.DatabaseException;
import bg.company.transport.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class JdbcEmployeeDao extends BaseDao implements EmployeeDao {
    public JdbcEmployeeDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Employee create(Employee employee) {
        String sql = "INSERT INTO employee(company_id, full_name, salary, role) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, employee.getCompanyId());
                ps.setString(2, employee.getFullName());
                ps.setBigDecimal(3, employee.getSalary());
                ps.setString(4, employee.getRole().name());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        employee.setId(rs.getLong(1));
                    }
                }
            }
            saveQualifications(con, employee.getId(), employee.getQualifications());
            con.commit();
            return employee;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create employee", e);
        }
    }

    @Override
    public Employee update(Employee employee) {
        String sql = "UPDATE employee SET company_id = ?, full_name = ?, salary = ?, role = ? WHERE id = ?";
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, employee.getCompanyId());
                ps.setString(2, employee.getFullName());
                ps.setBigDecimal(3, employee.getSalary());
                ps.setString(4, employee.getRole().name());
                ps.setLong(5, employee.getId());
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new NotFoundException("Employee not found: " + employee.getId());
                }
            }
            deleteQualifications(con, employee.getId());
            saveQualifications(con, employee.getId(), employee.getQualifications());
            con.commit();
            return employee;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update employee", e);
        }
    }

    @Override
    public Optional<Employee> findById(Long id) {
        String sql = "SELECT id, company_id, full_name, salary, role FROM employee WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee e = mapRow(rs);
                    e.setQualifications(findQualifications(id));
                    return Optional.of(e);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch employee", e);
        }
    }

    @Override
    public List<Employee> list(String qualificationFilter, String sort) {
        StringBuilder sql = new StringBuilder("SELECT id, company_id, full_name, salary, role FROM employee");
        List<Object> params = new ArrayList<>();
        if (qualificationFilter != null && !qualificationFilter.isBlank()) {
            sql.append(" WHERE id IN (SELECT employee_id FROM employee_qualification WHERE qualification = ?)");
            params.add(qualificationFilter);
        }
        String order = "salary".equalsIgnoreCase(sort) ? "salary" : "full_name";
        sql.append(" ORDER BY ").append(order);
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Employee> list = new ArrayList<>();
                while (rs.next()) {
                    Employee e = mapRow(rs);
                    e.setQualifications(findQualifications(e.getId()));
                    list.add(e);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list employees", e);
        }
    }

    @Override
    public Set<DriverQualification> findQualifications(Long employeeId) {
        String sql = "SELECT qualification FROM employee_qualification WHERE employee_id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                Set<DriverQualification> q = EnumSet.noneOf(DriverQualification.class);
                while (rs.next()) {
                    q.add(DriverQualification.valueOf(rs.getString("qualification")));
                }
                return q;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load qualifications", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM employee WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete employee", e);
        }
    }

    private void saveQualifications(Connection con, Long employeeId, Set<DriverQualification> qualifications) throws SQLException {
        if (qualifications == null || qualifications.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO employee_qualification(employee_id, qualification) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DriverQualification q : qualifications) {
                ps.setLong(1, employeeId);
                ps.setString(2, q.name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteQualifications(Connection con, Long employeeId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM employee_qualification WHERE employee_id = ?")) {
            ps.setLong(1, employeeId);
            ps.executeUpdate();
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getLong("id"));
        e.setCompanyId(rs.getLong("company_id"));
        e.setFullName(rs.getString("full_name"));
        e.setSalary(rs.getBigDecimal("salary"));
        e.setRole(EmployeeRole.valueOf(rs.getString("role")));
        return e;
    }
}
