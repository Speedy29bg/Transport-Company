package bg.company.transport.service;

import bg.company.transport.dao.EmployeeDao;
import bg.company.transport.dao.TransportDao;
import bg.company.transport.dao.VehicleDao;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import bg.company.transport.validation.TransportValidator;

import java.util.List;

public class TransportService {
    private final TransportDao transportDao;
    private final TransportValidator validator;

    public TransportService(TransportDao transportDao, EmployeeDao employeeDao, VehicleDao vehicleDao) {
        this.transportDao = transportDao;
        this.validator = new TransportValidator(vehicleDao, employeeDao);
    }

    public Transport create(Transport transport) {
        validator.validate(transport);
        return transportDao.create(transport);
    }

    public Transport update(Transport transport) {
        validator.validate(transport);
        return transportDao.update(transport);
    }

    public Transport get(Long id) {
        return transportDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Transport not found: " + id));
    }

    public List<Transport> list(String destinationFilter, String sort) {
        return transportDao.list(destinationFilter, sort);
    }

    public void setPaymentStatus(Long id, PaymentStatus status) {
        transportDao.updatePaymentStatus(id, status);
    }

    public void delete(Long id) {
        transportDao.delete(id);
    }
}
