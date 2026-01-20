package bg.company.transport.service;

import bg.company.transport.dao.ClientDao;
import bg.company.transport.domain.Client;
import bg.company.transport.exception.ValidationException;
import bg.company.transport.validation.ValidationResult;

import java.util.List;

public class ClientService {
    private final ClientDao clientDao;

    public ClientService(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public Client create(Client client) {
        validate(client);
        return clientDao.create(client);
    }

    public Client update(Client client) {
        validate(client);
        return clientDao.update(client);
    }

    public Client get(Long id) {
        return clientDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Client not found: " + id));
    }

    public List<Client> list() {
        return clientDao.list();
    }

    public void delete(Long id) {
        clientDao.delete(id);
    }

    private void validate(Client client) {
        ValidationResult vr = new ValidationResult();
        if (client.getName() == null || client.getName().isBlank()) {
            vr.addError("name", "Name is required");
        }
        if (client.getPhone() == null || client.getPhone().isBlank()) {
            vr.addError("phone", "Phone is required");
        }
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            vr.addError("email", "Email is required");
        }
        if (client.getType() == null) {
            vr.addError("type", "Type is required");
        }
        if (!vr.isValid()) {
            throw new ValidationException("Client validation failed", vr.getFieldErrors());
        }
    }
}
