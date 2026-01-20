package bg.company.transport.dao;

import bg.company.transport.domain.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleDao {
    Vehicle create(Vehicle vehicle);

    Vehicle update(Vehicle vehicle);

    Optional<Vehicle> findById(Long id);

    List<Vehicle> list(Long companyId);

    void delete(Long id);
}
