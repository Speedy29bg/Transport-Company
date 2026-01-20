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
import bg.company.transport.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportValidatorTest {

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
    void rejectsArrivalBeforeDeparture() {
        Transport transport = basePassengerTransport();
        transport.setDepartAt(OffsetDateTime.parse("2026-01-20T10:00:00Z"));
        transport.setArriveAt(OffsetDateTime.parse("2026-01-20T09:00:00Z"));
        stubBusAndQualifiedDriver(transport.getVehicleId(), transport.getDriverId(), 50);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(transport));
        assertEquals("Arrival must be after departure", ex.getFieldErrors().get("arriveAt"));
    }

    @Test
    void rejectsGoodsWithoutWeight() {
        Transport transport = baseGoodsTransport();
        stubTruckAndDriver(transport.getVehicleId(), transport.getDriverId(), 2000);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(transport));
        assertEquals("Weight must be > 0 for goods", ex.getFieldErrors().get("cargoWeightKg"));
    }

    @Test
    void rejectsPassengerCountOverCapacity() {
        Transport transport = basePassengerTransport();
        transport.setPassengerCount(60);
        stubBusAndQualifiedDriver(transport.getVehicleId(), transport.getDriverId(), 50);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(transport));
        String message = ex.getFieldErrors().get("passengerCount");
        assertEquals("Passenger count (60) exceeds vehicle capacity (50)", message);
    }

    @Test
    void requiresQualificationForLargePassengerCount() {
        Transport transport = basePassengerTransport();
        transport.setPassengerCount(20);
        Vehicle vehicle = new Vehicle(transport.getVehicleId(), 1L, "REG", VehicleType.BUS, 40, null, null);
        when(vehicleDao.findById(transport.getVehicleId())).thenReturn(Optional.of(vehicle));

        Employee driver = new Employee(transport.getDriverId(), 1L, "Driver", BigDecimal.valueOf(1000), EmployeeRole.DRIVER, EnumSet.noneOf(DriverQualification.class));
        when(employeeDao.findById(transport.getDriverId())).thenReturn(Optional.of(driver));

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(transport));
        assertEquals("Driver must have PASSENGERS_12_PLUS qualification for >12 passengers", ex.getFieldErrors().get("driverId"));
    }

    private Transport basePassengerTransport() {
        Transport transport = new Transport();
        transport.setCompanyId(1L);
        transport.setClientId(1L);
        transport.setOrigin("Sofia");
        transport.setDestination("Plovdiv");
        transport.setDepartAt(OffsetDateTime.parse("2026-01-20T08:00:00Z"));
        transport.setArriveAt(OffsetDateTime.parse("2026-01-20T10:00:00Z"));
        transport.setCargoKind(CargoKind.PASSENGERS);
        transport.setPassengerCount(10);
        transport.setPrice(BigDecimal.valueOf(150));
        transport.setDriverId(10L);
        transport.setVehicleId(20L);
        transport.setPaymentStatus(PaymentStatus.UNPAID);
        return transport;
    }

    private Transport baseGoodsTransport() {
        Transport transport = new Transport();
        transport.setCompanyId(1L);
        transport.setClientId(1L);
        transport.setOrigin("Sofia");
        transport.setDestination("Burgas");
        transport.setDepartAt(OffsetDateTime.parse("2026-01-20T08:00:00Z"));
        transport.setArriveAt(OffsetDateTime.parse("2026-01-20T12:00:00Z"));
        transport.setCargoKind(CargoKind.GOODS);
        transport.setCargoWeightKg(null);
        transport.setPrice(BigDecimal.valueOf(500));
        transport.setDriverId(30L);
        transport.setVehicleId(40L);
        transport.setPaymentStatus(PaymentStatus.UNPAID);
        return transport;
    }

    private void stubBusAndQualifiedDriver(Long vehicleId, Long driverId, int seatCount) {
        Vehicle vehicle = new Vehicle(vehicleId, 1L, "BUS1", VehicleType.BUS, seatCount, null, null);
        when(vehicleDao.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        Employee driver = new Employee(driverId, 1L, "Driver", BigDecimal.valueOf(1200), EmployeeRole.DRIVER,
                EnumSet.of(DriverQualification.PASSENGERS_12_PLUS));
        when(employeeDao.findById(driverId)).thenReturn(Optional.of(driver));
    }

    private void stubTruckAndDriver(Long vehicleId, Long driverId, int maxLoadKg) {
        Vehicle vehicle = new Vehicle(vehicleId, 1L, "TRK1", VehicleType.TRUCK, null, maxLoadKg, null);
        when(vehicleDao.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        Employee driver = new Employee(driverId, 1L, "Driver", BigDecimal.valueOf(1200), EmployeeRole.DRIVER,
                EnumSet.noneOf(DriverQualification.class));
        when(employeeDao.findById(driverId)).thenReturn(Optional.of(driver));
    }
}
