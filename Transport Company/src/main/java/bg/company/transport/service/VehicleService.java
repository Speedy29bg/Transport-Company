package bg.company.transport.service;

import bg.company.transport.dao.VehicleDao;
import bg.company.transport.domain.Vehicle;
import bg.company.transport.validation.VehicleValidator;

import java.util.List;

public class VehicleService {
    private final VehicleDao vehicleDao;
    private final VehicleValidator validator = new VehicleValidator();

    public VehicleService(VehicleDao vehicleDao) {
        this.vehicleDao = vehicleDao;
    }

    public Vehicle create(Vehicle vehicle) {
        validator.validate(vehicle);
        return vehicleDao.create(vehicle);
    }

    public Vehicle update(Vehicle vehicle) {
        validator.validate(vehicle);
        return vehicleDao.update(vehicle);
    }

    public Vehicle get(Long id) {
        return vehicleDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
    }

    public List<Vehicle> list(Long companyId) {
        return vehicleDao.list(companyId);
    }

    public void delete(Long id) {
        vehicleDao.delete(id);
    }
}
