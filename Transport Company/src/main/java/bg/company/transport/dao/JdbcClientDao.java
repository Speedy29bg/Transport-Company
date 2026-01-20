package bg.company.transport.dao;

import bg.company.transport.domain.Client;
import bg.company.transport.domain.ClientType;
import bg.company.transport.exception.DatabaseException;
import bg.company.transport.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcClientDao extends BaseDao implements ClientDao {
    public JdbcClientDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Client create(Client client) {
        String sql = "INSERT INTO client(name, phone, email, type) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getType().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    client.setId(rs.getLong(1));
                }
            }
            return client;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create client", e);
        }
    }

    @Override
    public Client update(Client client) {
        String sql = "UPDATE client SET name = ?, phone = ?, email = ?, type = ? WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getType().name());
            ps.setLong(5, client.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new NotFoundException("Client not found: " + client.getId());
            }
            return client;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update client", e);
        }
    }

    @Override
    public Optional<Client> findById(Long id) {
        String sql = "SELECT id, name, phone, email, type FROM client WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch client", e);
        }
    }

    @Override
    public List<Client> list() {
        String sql = "SELECT id, name, phone, email, type FROM client ORDER BY name";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Client> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list clients", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM client WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete client", e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getLong("id"));
        client.setName(rs.getString("name"));
        client.setPhone(rs.getString("phone"));
        client.setEmail(rs.getString("email"));
        client.setType(ClientType.valueOf(rs.getString("type")));
        return client;
    }
}
