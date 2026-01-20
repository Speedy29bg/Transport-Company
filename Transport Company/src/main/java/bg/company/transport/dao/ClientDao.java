package bg.company.transport.dao;

import bg.company.transport.domain.Client;

import java.util.List;
import java.util.Optional;

public interface ClientDao {
    Client create(Client client);

    Client update(Client client);

    Optional<Client> findById(Long id);

    List<Client> list();

    void delete(Long id);
}
