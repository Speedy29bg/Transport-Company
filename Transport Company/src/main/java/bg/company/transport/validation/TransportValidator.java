package bg.company.transport.validation;

import bg.company.transport.dao.EmployeeDao;
import bg.company.transport.dao.VehicleDao;
import bg.company.transport.domain.*;
import bg.company.transport.exception.ValidationException;

import java.time.OffsetDateTime;

public class TransportValidator {
    private final VehicleDao vehicleDao;
    private final EmployeeDao employeeDao;

    public TransportValidator(VehicleDao vehicleDao, EmployeeDao employeeDao) {
        this.vehicleDao = vehicleDao;
        this.employeeDao = employeeDao;
    }

    public void validate(Transport transport) {
        ValidationResult result = new ValidationResult();
        
        // Basic field validations
        if (transport.getCompanyId() == null) {
            result.addError("companyId", "Company is required");
        }
        if (transport.getClientId() == null) {
            result.addError("clientId", "Client is required");
        }
        if (transport.getOrigin() == null || transport.getOrigin().isBlank()) {
            result.addError("origin", "Origin is required");
        }
        if (transport.getDestination() == null || transport.getDestination().isBlank()) {
            result.addError("destination", "Destination is required");
        }
        
        // Date validation: arriveAt >= departAt
        OffsetDateTime depart = transport.getDepartAt();
        OffsetDateTime arrive = transport.getArriveAt();
        if (depart == null) {
            result.addError("departAt", "Departure date is required");
        }
        if (arrive == null) {
            result.addError("arriveAt", "Arrival date is required");
        }
        if (depart != null && arrive != null && arrive.isBefore(depart)) {
            result.addError("arriveAt", "Arrival must be after departure");
        }
        
        // Cargo kind validation
        if (transport.getCargoKind() == null) {
            result.addError("cargoKind", "Cargo kind is required");
        }
        
        // Cargo constraints: GOODS requires weight, PASSENGERS requires count
        if (transport.getCargoKind() == CargoKind.GOODS) {
            if (transport.getCargoWeightKg() == null || transport.getCargoWeightKg() <= 0) {
                result.addError("cargoWeightKg", "Weight must be > 0 for goods");
            }
            if (transport.getPassengerCount() != null) {
                result.addError("passengerCount", "Passenger count must be null for goods");
            }
        }
        if (transport.getCargoKind() == CargoKind.PASSENGERS) {
            if (transport.getPassengerCount() == null || transport.getPassengerCount() <= 0) {
                result.addError("passengerCount", "Passenger count must be > 0 for passengers");
            }
            if (transport.getCargoWeightKg() != null) {
                result.addError("cargoWeightKg", "Weight must be null for passengers");
            }
        }
        
        // Price validation
        if (transport.getPrice() == null || transport.getPrice().signum() <= 0) {
            result.addError("price", "Price must be > 0");
        }
        
        // Vehicle validation and compatibility check
        if (transport.getVehicleId() == null) {
            result.addError("vehicleId", "Vehicle is required");
        } else {
            vehicleDao.findById(transport.getVehicleId()).ifPresentOrElse(
                vehicle -> validateVehicleCompatibility(transport, vehicle, result),
                () -> result.addError("vehicleId", "Vehicle not found: " + transport.getVehicleId())
            );
        }
        
        // Driver validation and qualifications check
        if (transport.getDriverId() == null) {
            result.addError("driverId", "Driver is required");
        } else {
            employeeDao.findById(transport.getDriverId()).ifPresentOrElse(
                driver -> validateDriverQualifications(transport, driver, result),
                () -> result.addError("driverId", "Driver not found: " + transport.getDriverId())
            );
        }
        
        // Payment status validation
        if (transport.getPaymentStatus() == null) {
            result.addError("paymentStatus", "Payment status is required");
        }
        
        if (!result.isValid()) {
            throw new ValidationException("Transport validation failed", result.getFieldErrors());
        }
    }
    
    private void validateVehicleCompatibility(Transport transport, Vehicle vehicle, ValidationResult result) {
        // Check vehicle type matches cargo kind
        if (transport.getCargoKind() == CargoKind.PASSENGERS) {
            if (vehicle.getType() != VehicleType.BUS) {
                result.addError("vehicleId", "Only BUS vehicles can transport passengers");
            }
            // Check if vehicle has enough seats
            if (vehicle.getSeatCount() != null && transport.getPassengerCount() != null) {
                if (transport.getPassengerCount() > vehicle.getSeatCount()) {
                    result.addError("passengerCount", "Passenger count (" + transport.getPassengerCount() + 
                        ") exceeds vehicle capacity (" + vehicle.getSeatCount() + ")");
                }
            }
        } else if (transport.getCargoKind() == CargoKind.GOODS) {
            if (vehicle.getType() == VehicleType.BUS) {
                result.addError("vehicleId", "BUS vehicles cannot transport goods");
            }
            // Check weight capacity for trucks
            if (vehicle.getType() == VehicleType.TRUCK && vehicle.getMaxLoadKg() != null && 
                transport.getCargoWeightKg() != null) {
                if (transport.getCargoWeightKg() > vehicle.getMaxLoadKg()) {
                    result.addError("cargoWeightKg", "Cargo weight (" + transport.getCargoWeightKg() + 
                        "kg) exceeds vehicle capacity (" + vehicle.getMaxLoadKg() + "kg)");
                }
            }
        }
    }
    
    private void validateDriverQualifications(Transport transport, Employee driver, ValidationResult result) {
        // Check if employee is actually a driver
        if (driver.getRole() != EmployeeRole.DRIVER) {
            result.addError("driverId", "Employee must be a DRIVER (current role: " + driver.getRole() + ")");
            return;
        }
        
        // Check if driver has required qualifications for passenger transport
        if (transport.getCargoKind() == CargoKind.PASSENGERS && transport.getPassengerCount() != null) {
            if (transport.getPassengerCount() > 12) {
                if (!driver.getQualifications().contains(DriverQualification.PASSENGERS_12_PLUS)) {
                    result.addError("driverId", "Driver must have PASSENGERS_12_PLUS qualification for >12 passengers");
                }
            }
        }
        
        // Note: HAZMAT qualification check would go here if we had hazardous cargo type
        // For now, we only have GOODS and PASSENGERS as CargoKind
    }
}
