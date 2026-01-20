package bg.company.transport.dao;

import bg.company.transport.domain.TransportCompany;

import java.util.List;
import java.util.Optional;

public interface TransportCompanyDao {
    TransportCompany create(TransportCompany company);

    TransportCompany update(TransportCompany company);

    Optional<TransportCompany> findById(Long id);

    List<TransportCompany> list(String sort);

    void delete(Long id);
}
