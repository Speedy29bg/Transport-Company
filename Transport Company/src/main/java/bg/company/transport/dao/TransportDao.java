package bg.company.transport.dao;

import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;

import java.util.List;
import java.util.Optional;

public interface TransportDao {
    Transport create(Transport transport);

    Transport update(Transport transport);

    Optional<Transport> findById(Long id);

    List<Transport> list(String destinationFilter, String sort);

    void updatePaymentStatus(Long id, PaymentStatus status);

    void delete(Long id);
}
