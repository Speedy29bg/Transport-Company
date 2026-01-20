package bg.company.transport.dao;

import bg.company.transport.domain.TransportCompany;
import bg.company.transport.exception.DatabaseException;
import bg.company.transport.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTransportCompanyDao extends BaseDao implements TransportCompanyDao {

    public JdbcTransportCompanyDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public TransportCompany create(TransportCompany company) {
        String sql = "INSERT INTO transport_company(name, address, created_at) VALUES (?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, company.getName());
            ps.setString(2, company.getAddress());
            ps.setTimestamp(3, Timestamp.from(company.getCreatedAt().toInstant()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    company.setId(rs.getLong(1));
                }
            }
            return company;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create company", e);
        }
    }

    @Override
    public TransportCompany update(TransportCompany company) {
        String sql = "UPDATE transport_company SET name = ?, address = ? WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, company.getName());
            ps.setString(2, company.getAddress());
            ps.setLong(3, company.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new NotFoundException("Company not found: " + company.getId());
            }
            return company;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update company", e);
        }
    }

    @Override
    public Optional<TransportCompany> findById(Long id) {
        String sql = "SELECT id, name, address, created_at FROM transport_company WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch company", e);
        }
    }

    @Override
    public List<TransportCompany> list(String sort) {
        String orderBy = "name";
        if ("revenue".equalsIgnoreCase(sort)) {
            orderBy = "revenue";
        }
        String sql = "SELECT c.id, c.name, c.address, c.created_at, COALESCE(SUM(t.price),0) AS revenue " +
                "FROM transport_company c LEFT JOIN transport t ON t.company_id = c.id GROUP BY c.id, c.name, c.address, c.created_at " +
                "ORDER BY " + orderBy + ("revenue".equals(orderBy) ? " DESC" : " ASC");
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<TransportCompany> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list companies", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM transport_company WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete company", e);
        }
    }

    private TransportCompany mapRow(ResultSet rs) throws SQLException {
        TransportCompany company = new TransportCompany();
        company.setId(rs.getLong("id"));
        company.setName(rs.getString("name"));
        company.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            company.setCreatedAt(ts.toInstant().atOffset(ZoneOffset.UTC));
        }
        return company;
    }
}
