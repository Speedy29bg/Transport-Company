package bg.company.transport.dao;

import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.exception.DatabaseException;
import bg.company.transport.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcVehicleDao extends BaseDao implements VehicleDao {
    public JdbcVehicleDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Vehicle create(Vehicle vehicle) {
        String sql = "INSERT INTO vehicle(company_id, reg_number, type, seat_count, max_load_kg, volume_liters) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(vehicle, ps);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    vehicle.setId(rs.getLong(1));
                }
            }
            return vehicle;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create vehicle", e);
        }
    }

    @Override
    public Vehicle update(Vehicle vehicle) {
        String sql = "UPDATE vehicle SET company_id = ?, reg_number = ?, type = ?, seat_count = ?, max_load_kg = ?, volume_liters = ? WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            fillStatement(vehicle, ps);
            ps.setLong(7, vehicle.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new NotFoundException("Vehicle not found: " + vehicle.getId());
            }
            return vehicle;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update vehicle", e);
        }
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        String sql = "SELECT id, company_id, reg_number, type, seat_count, max_load_kg, volume_liters FROM vehicle WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch vehicle", e);
        }
    }

    @Override
    public List<Vehicle> list(Long companyId) {
        String sql = "SELECT id, company_id, reg_number, type, seat_count, max_load_kg, volume_liters FROM vehicle WHERE company_id = ? ORDER BY reg_number";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Vehicle> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list vehicles", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM vehicle WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete vehicle", e);
        }
    }

    private void fillStatement(Vehicle vehicle, PreparedStatement ps) throws SQLException {
        ps.setLong(1, vehicle.getCompanyId());
        ps.setString(2, vehicle.getRegNumber());
        ps.setString(3, vehicle.getType().name());
        ps.setObject(4, vehicle.getSeatCount());
        ps.setObject(5, vehicle.getMaxLoadKg());
        ps.setObject(6, vehicle.getVolumeLiters());
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getLong("id"));
        v.setCompanyId(rs.getLong("company_id"));
        v.setRegNumber(rs.getString("reg_number"));
        v.setType(VehicleType.valueOf(rs.getString("type")));
        v.setSeatCount((Integer) rs.getObject("seat_count"));
        v.setMaxLoadKg((Integer) rs.getObject("max_load_kg"));
        v.setVolumeLiters((Integer) rs.getObject("volume_liters"));
        return v;
    }
}
