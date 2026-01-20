package bg.company.transport.reporting;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import bg.company.transport.exception.DatabaseException;

public class ReportingService {
    private final DataSource dataSource;

    public ReportingService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long totalTransports() {
        String sql = "SELECT COUNT(*) FROM transport";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get total transports", e);
        }
    }

    public BigDecimal totalRevenue(boolean onlyPaid) {
        String sql = "SELECT COALESCE(SUM(price),0) FROM transport" + (onlyPaid ? " WHERE payment_status = 'PAID'" : "");
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get revenue", e);
        }
    }

    public Map<Long, Long> driverTripsCount() {
        String sql = "SELECT driver_id, COUNT(*) as trips FROM transport GROUP BY driver_id";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            Map<Long, Long> map = new LinkedHashMap<>();
            while (rs.next()) {
                map.put(rs.getLong("driver_id"), rs.getLong("trips"));
            }
            return map;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get driver trips", e);
        }
    }

    public BigDecimal revenueForPeriod(LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(price),0) FROM transport WHERE depart_at >= ? AND arrive_at <= ?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, from.atStartOfDay());
            ps.setObject(2, to.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get period revenue", e);
        }
    }

    public Map<Long, BigDecimal> revenuePerDriver(LocalDate from, LocalDate to) {
        String sql = "SELECT driver_id, COALESCE(SUM(price),0) AS revenue FROM transport WHERE depart_at >= ? AND arrive_at <= ? GROUP BY driver_id";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, from.atStartOfDay());
            ps.setObject(2, to.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                Map<Long, BigDecimal> map = new LinkedHashMap<>();
                while (rs.next()) {
                    map.put(rs.getLong("driver_id"), rs.getBigDecimal("revenue"));
                }
                return map;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get revenue per driver", e);
        }
    }
}
