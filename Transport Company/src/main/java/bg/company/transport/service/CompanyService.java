package bg.company.transport.service;

import bg.company.transport.dao.TransportCompanyDao;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.exception.ValidationException;
import bg.company.transport.validation.ValidationResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class CompanyService {
    private final TransportCompanyDao companyDao;

    public CompanyService(TransportCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    public TransportCompany create(TransportCompany company) {
        validate(company);
        company.setCreatedAt(OffsetDateTime.now());
        return companyDao.create(company);
    }

    public TransportCompany update(TransportCompany company) {
        validate(company);
        return companyDao.update(company);
    }

    public TransportCompany get(Long id) {
        return companyDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));
    }

    public List<TransportCompany> list(String sort) {
        return companyDao.list(sort);
    }

    public void delete(Long id) {
        companyDao.delete(id);
    }

    private void validate(TransportCompany company) {
        ValidationResult vr = new ValidationResult();
        if (company.getName() == null || company.getName().isBlank()) {
            vr.addError("name", "Name is required");
        }
        if (company.getAddress() == null || company.getAddress().isBlank()) {
            vr.addError("address", "Address is required");
        }
        if (!vr.isValid()) {
            throw new ValidationException("Company validation failed", vr.getFieldErrors());
        }
    }
}
