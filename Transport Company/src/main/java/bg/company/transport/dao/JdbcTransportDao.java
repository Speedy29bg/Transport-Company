package bg.company.transport.dao;

import bg.company.transport.domain.CargoKind;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import bg.company.transport.exception.DatabaseException;
import bg.company.transport.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTransportDao extends BaseDao implements TransportDao {
    public JdbcTransportDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Transport create(Transport transport) {
        String sql = "INSERT INTO transport(company_id, client_id, origin, destination, depart_at, arrive_at, cargo_kind, cargo_weight_kg, passenger_count, price, driver_id, vehicle_id, payment_status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(transport, ps);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    transport.setId(rs.getLong(1));
                }
            }
            return transport;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create transport", e);
        }
    }

    @Override
    public Transport update(Transport transport) {
        String sql = "UPDATE transport SET company_id = ?, client_id = ?, origin = ?, destination = ?, depart_at = ?, arrive_at = ?, cargo_kind = ?, cargo_weight_kg = ?, passenger_count = ?, price = ?, driver_id = ?, vehicle_id = ?, payment_status = ? WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            fillStatement(transport, ps);
            ps.setLong(14, transport.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new NotFoundException("Transport not found: " + transport.getId());
            }
            return transport;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update transport", e);
        }
    }

    @Override
    public Optional<Transport> findById(Long id) {
        String sql = baseSelect() + " WHERE t.id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch transport", e);
        }
    }

    @Override
    public List<Transport> list(String destinationFilter, String sort) {
        StringBuilder sql = new StringBuilder(baseSelect());
        List<Object> params = new ArrayList<>();
        if (destinationFilter != null && !destinationFilter.isBlank()) {
            sql.append(" WHERE t.destination LIKE ?");
            params.add("%" + destinationFilter + "%");
        }
        String order = "depart".equalsIgnoreCase(sort) ? "t.depart_at" : "t.created_at";
        sql.append(" ORDER BY ").append(order);
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Transport> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list transports", e);
        }
    }

    @Override
    public void updatePaymentStatus(Long id, PaymentStatus status) {
        String sql = "UPDATE transport SET payment_status = ? WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new NotFoundException("Transport not found: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update payment status", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM transport WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete transport", e);
        }
    }

    private String baseSelect() {
        return "SELECT t.id, t.company_id, t.client_id, t.origin, t.destination, t.depart_at, t.arrive_at, t.cargo_kind, t.cargo_weight_kg, t.passenger_count, t.price, t.driver_id, t.vehicle_id, t.payment_status, t.created_at FROM transport t";
    }

    private void fillStatement(Transport transport, PreparedStatement ps) throws SQLException {
        ps.setLong(1, transport.getCompanyId());
        ps.setLong(2, transport.getClientId());
        ps.setString(3, transport.getOrigin());
        ps.setString(4, transport.getDestination());
        ps.setTimestamp(5, Timestamp.from(transport.getDepartAt().toInstant()));
        ps.setTimestamp(6, Timestamp.from(transport.getArriveAt().toInstant()));
        ps.setString(7, transport.getCargoKind().name());
        ps.setObject(8, transport.getCargoWeightKg());
        ps.setObject(9, transport.getPassengerCount());
        ps.setBigDecimal(10, transport.getPrice());
        ps.setLong(11, transport.getDriverId());
        ps.setLong(12, transport.getVehicleId());
        ps.setString(13, transport.getPaymentStatus().name());
        ps.setTimestamp(14, Timestamp.from(transport.getCreatedAt().toInstant()));
    }

    private Transport mapRow(ResultSet rs) throws SQLException {
        Transport t = new Transport();
        t.setId(rs.getLong("id"));
        t.setCompanyId(rs.getLong("company_id"));
        t.setClientId(rs.getLong("client_id"));
        t.setOrigin(rs.getString("origin"));
        t.setDestination(rs.getString("destination"));
        Timestamp depart = rs.getTimestamp("depart_at");
        if (depart != null) {
            t.setDepartAt(depart.toInstant().atOffset(ZoneOffset.UTC));
        }
        Timestamp arrive = rs.getTimestamp("arrive_at");
        if (arrive != null) {
            t.setArriveAt(arrive.toInstant().atOffset(ZoneOffset.UTC));
        }
        t.setCargoKind(CargoKind.valueOf(rs.getString("cargo_kind")));
        t.setCargoWeightKg((Integer) rs.getObject("cargo_weight_kg"));
        t.setPassengerCount((Integer) rs.getObject("passenger_count"));
        t.setPrice(rs.getBigDecimal("price"));
        t.setDriverId(rs.getLong("driver_id"));
        t.setVehicleId(rs.getLong("vehicle_id"));
        t.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            t.setCreatedAt(created.toInstant().atOffset(ZoneOffset.UTC));
        }
        return t;
    }
}
