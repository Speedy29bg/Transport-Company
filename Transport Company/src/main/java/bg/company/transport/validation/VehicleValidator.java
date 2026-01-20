package bg.company.transport.validation;

import bg.company.transport.domain.Vehicle;
import bg.company.transport.domain.VehicleType;
import bg.company.transport.exception.ValidationException;

public class VehicleValidator {
    public void validate(Vehicle vehicle) {
        ValidationResult result = new ValidationResult();
        
        // Basic field validation
        if (vehicle.getCompanyId() == null) {
            result.addError("companyId", "Company is required");
        }
        if (vehicle.getRegNumber() == null || vehicle.getRegNumber().isBlank()) {
            result.addError("regNumber", "Registration number is required");
        }
        if (vehicle.getType() == null) {
            result.addError("type", "Vehicle type is required");
            if (!result.isValid()) {
                throw new ValidationException("Vehicle validation failed", result.getFieldErrors());
            }
            return;
        }
        
        // Type-specific validation
        VehicleType type = vehicle.getType();
        switch (type) {
            case BUS:
                if (vehicle.getSeatCount() == null || vehicle.getSeatCount() <= 0) {
                    result.addError("seatCount", "Seat count must be > 0 for buses");
                }
                if (vehicle.getMaxLoadKg() != null) {
                    result.addError("maxLoadKg", "Max load must be null for buses");
                }
                if (vehicle.getVolumeLiters() != null) {
                    result.addError("volumeLiters", "Volume must be null for buses");
                }
                break;
            case TRUCK:
                if (vehicle.getMaxLoadKg() == null || vehicle.getMaxLoadKg() <= 0) {
                    result.addError("maxLoadKg", "Max load must be > 0 for trucks");
                }
                if (vehicle.getSeatCount() != null) {
                    result.addError("seatCount", "Seat count must be null for trucks");
                }
                if (vehicle.getVolumeLiters() != null) {
                    result.addError("volumeLiters", "Volume must be null for trucks");
                }
                break;
            case TANKER:
                if (vehicle.getVolumeLiters() == null || vehicle.getVolumeLiters() <= 0) {
                    result.addError("volumeLiters", "Volume must be > 0 for tankers");
                }
                if (vehicle.getSeatCount() != null) {
                    result.addError("seatCount", "Seat count must be null for tankers");
                }
                if (vehicle.getMaxLoadKg() != null) {
                    result.addError("maxLoadKg", "Max load must be null for tankers");
                }
                break;
            case VAN:
                // Van can have both maxLoadKg and seatCount, but at least one should be present
                if ((vehicle.getMaxLoadKg() == null || vehicle.getMaxLoadKg() <= 0) &&
                    (vehicle.getSeatCount() == null || vehicle.getSeatCount() <= 0)) {
                    result.addError("maxLoadKg", "Van must have either max load or seat count");
                }
                if (vehicle.getVolumeLiters() != null) {
                    result.addError("volumeLiters", "Volume must be null for vans");
                }
                break;
        }
        
        if (!result.isValid()) {
            throw new ValidationException("Vehicle validation failed", result.getFieldErrors());
        }
    }
}
