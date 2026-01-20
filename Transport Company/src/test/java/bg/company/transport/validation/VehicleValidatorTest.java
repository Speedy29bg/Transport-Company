package bg.company.transport.validation;

import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleValidatorTest {

    private final VehicleValidator validator = new VehicleValidator();

    @Test
    void busRequiresSeatCount() {
        Vehicle vehicle = new Vehicle();
        vehicle.setCompanyId(1L);
        vehicle.setRegNumber("BUS-01");
        vehicle.setType(VehicleType.BUS);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validate(vehicle));
        assertEquals("Seat count must be > 0 for buses", ex.getFieldErrors().get("seatCount"));
    }
}
