package bg.company.transport.validation;

import bg.company.transport.dao.EmployeeDao;
import bg.company.transport.dao.VehicleDao;
import bg.company.transport.domain.CargoKind;
import bg.company.transport.domain.DriverQualification;
import bg.company.transport.domain.Employee;
import bg.company.transport.domain.EmployeeRole;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportValidatorSmokeTest {

    @Mock
    private VehicleDao vehicleDao;

    @Mock
    private EmployeeDao employeeDao;

    private TransportValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransportValidator(vehicleDao, employeeDao);
    }

    @Test
    void acceptsValidPassengerTransport() {
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
        transport.setDriverId(10L);
        transport.setVehicleId(20L);
        transport.setPaymentStatus(PaymentStatus.UNPAID);

        Vehicle vehicle = new Vehicle(20L, 1L, "BUS1", VehicleType.BUS, 50, null, null);
        when(vehicleDao.findById(20L)).thenReturn(Optional.of(vehicle));

        Employee driver = new Employee(10L, 1L, "Driver", BigDecimal.valueOf(1500), EmployeeRole.DRIVER,
                EnumSet.of(DriverQualification.PASSENGERS_12_PLUS));
        when(employeeDao.findById(10L)).thenReturn(Optional.of(driver));

        assertDoesNotThrow(() -> validator.validate(transport));
    }

    @Test
    void acceptsValidGoodsTransport() {
        Transport transport = new Transport();
        transport.setCompanyId(1L);
        transport.setClientId(1L);
        transport.setOrigin("Varna");
        transport.setDestination("Burgas");
        transport.setDepartAt(OffsetDateTime.parse("2026-02-01T08:00:00Z"));
        transport.setArriveAt(OffsetDateTime.parse("2026-02-01T12:00:00Z"));
        transport.setCargoKind(CargoKind.GOODS);
        transport.setCargoWeightKg(1200);
        transport.setPrice(BigDecimal.valueOf(900));
        transport.setDriverId(30L);
        transport.setVehicleId(40L);
        transport.setPaymentStatus(PaymentStatus.PAID);

        Vehicle vehicle = new Vehicle(40L, 1L, "TRK1", VehicleType.TRUCK, null, 2000, null);
        when(vehicleDao.findById(40L)).thenReturn(Optional.of(vehicle));

        Employee driver = new Employee(30L, 1L, "Driver", BigDecimal.valueOf(1500), EmployeeRole.DRIVER,
                EnumSet.noneOf(DriverQualification.class));
        when(employeeDao.findById(30L)).thenReturn(Optional.of(driver));

        assertDoesNotThrow(() -> validator.validate(transport));
    }
}
