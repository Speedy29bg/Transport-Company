package bg.company.transport.dao;

import bg.company.transport.domain.CargoKind;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransportDaoIntegrationTest {

    private static MySQLContainer<?> mysql;
    private static HikariDataSource dataSource;
    private static TransportDao transportDao;

    @BeforeAll
    static void init() {
    Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available; skipping integration test");

    mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("transport_company")
        .withUsername("test")
        .withPassword("test");
    mysql.start();

        HikariConfig cfg = new HikariConfig();
    cfg.setJdbcUrl(mysql.getJdbcUrl());
    cfg.setUsername(mysql.getUsername());
    cfg.setPassword(mysql.getPassword());
    cfg.setDriverClassName(mysql.getDriverClassName());
        cfg.setMaximumPoolSize(5);
        dataSource = new HikariDataSource(cfg);

        Flyway.configure()
                .dataSource(dataSource)
                .schemas("transport_company")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        seedReferenceData(dataSource);
        transportDao = new JdbcTransportDao(dataSource);
    }

    @AfterAll
    static void cleanup() {
        if (dataSource != null) {
            dataSource.close();
        }
        if (mysql != null && mysql.isRunning()) {
            mysql.stop();
        }
    }

    @Test
    void createsAndReadsTransport() {
        Transport transport = new Transport();
        transport.setCompanyId(1L);
        transport.setClientId(1L);
        transport.setOrigin("Sofia");
        transport.setDestination("Plovdiv");
        transport.setDepartAt(OffsetDateTime.parse("2026-01-20T08:00:00Z"));
        transport.setArriveAt(OffsetDateTime.parse("2026-01-20T10:00:00Z"));
        transport.setCargoKind(CargoKind.PASSENGERS);
        transport.setPassengerCount(20);
        transport.setPrice(BigDecimal.valueOf(200));
        transport.setDriverId(1L);
        transport.setVehicleId(1L);
        transport.setPaymentStatus(PaymentStatus.UNPAID);
        transport.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Transport created = transportDao.create(transport);
        assertNotNull(created.getId());

        Transport fetched = transportDao.findById(created.getId()).orElseThrow();
        assertEquals("Plovdiv", fetched.getDestination());
        assertEquals(PaymentStatus.UNPAID, fetched.getPaymentStatus());
    }

    @Test
    void updatesPaymentStatus() {
        Transport transport = new Transport();
        transport.setCompanyId(1L);
        transport.setClientId(1L);
        transport.setOrigin("Varna");
        transport.setDestination("Burgas");
        transport.setDepartAt(OffsetDateTime.parse("2026-02-01T08:00:00Z"));
        transport.setArriveAt(OffsetDateTime.parse("2026-02-01T12:00:00Z"));
        transport.setCargoKind(CargoKind.GOODS);
        transport.setCargoWeightKg(500);
        transport.setPrice(BigDecimal.valueOf(800));
        transport.setDriverId(1L);
        transport.setVehicleId(2L);
        transport.setPaymentStatus(PaymentStatus.UNPAID);
        transport.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Transport created = transportDao.create(transport);
        transportDao.updatePaymentStatus(created.getId(), PaymentStatus.PAID);

        Transport fetched = transportDao.findById(created.getId()).orElseThrow();
        assertEquals(PaymentStatus.PAID, fetched.getPaymentStatus());
    }

    @Test
    void listsWithDestinationFilter() {
        // Seed one transport to filter on
        Transport transport = new Transport();
        transport.setCompanyId(1L);
        transport.setClientId(1L);
        transport.setOrigin("Sofia");
        transport.setDestination("Burgas Center");
        transport.setDepartAt(OffsetDateTime.parse("2026-03-01T08:00:00Z"));
        transport.setArriveAt(OffsetDateTime.parse("2026-03-01T12:00:00Z"));
        transport.setCargoKind(CargoKind.GOODS);
        transport.setCargoWeightKg(1000);
        transport.setPrice(BigDecimal.valueOf(900));
        transport.setDriverId(1L);
        transport.setVehicleId(2L);
        transport.setPaymentStatus(PaymentStatus.UNPAID);
        transport.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        transportDao.create(transport);

        List<Transport> filtered = transportDao.list("Burgas", "depart");
        assertEquals(1, filtered.size());
        assertEquals("Burgas Center", filtered.get(0).getDestination());
    }

    private static void seedReferenceData(DataSource ds) {
        try (Connection con = ds.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO transport_company(name, address) VALUES ('Co', 'Addr')")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO client(company_id, name, phone, email, type) VALUES (1, 'Client', '123', 'c@example.com', 'PERSON')")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO employee(company_id, full_name, salary, role) VALUES (1, 'Driver', 1000, 'DRIVER')")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO vehicle(company_id, reg_number, type, seat_count, max_load_kg, volume_liters) VALUES (1, 'BUS1', 'BUS', 50, NULL, NULL)")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO vehicle(company_id, reg_number, type, seat_count, max_load_kg, volume_liters) VALUES (1, 'TRK1', 'TRUCK', NULL, 5000, NULL)")) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed reference data", e);
        }
    }
}
