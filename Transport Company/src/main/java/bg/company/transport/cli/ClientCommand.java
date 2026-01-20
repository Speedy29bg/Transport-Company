package bg.company.transport.cli;

import bg.company.transport.dao.JdbcClientDao;
import bg.company.transport.domain.Client;
import bg.company.transport.domain.ClientType;
import bg.company.transport.service.ClientService;
import bg.company.transport.config.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "client", description = "Manage clients")
public class ClientCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientCommand.class);
    private final ClientService clientService;

    public ClientCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var dao = new JdbcClientDao(dataSource);
            this.clientService = new ClientService(dao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ClientCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Client management. Use: client add|edit|delete|get|list");
    }

    @Command(name = "add", description = "Add a new client")
    public void add(
            @Parameters(index = "0", description = "Client name") String name,
            @Parameters(index = "1", description = "Phone") String phone,
            @Parameters(index = "2", description = "Email") String email,
            @Option(names = {"-t", "--type"}, description = "Type: PERSON|COMPANY", defaultValue = "PERSON") String type) {
        try {
            Client client = new Client();
            client.setName(name);
            client.setPhone(phone);
            client.setEmail(email);
            client.setType(ClientType.valueOf(type.toUpperCase()));
            Client created = clientService.create(client);
            System.out.println("Client created successfully with ID: " + created.getId());
        } catch (Exception e) {
            System.err.println("Error creating client: " + e.getMessage());
            logger.error("Failed to create client", e);
        }
    }

    @Command(name = "list", description = "List all clients")
    public void list() {
        try {
            List<Client> clients = clientService.list();
            if (clients.isEmpty()) {
                System.out.println("No clients found");
            } else {
                System.out.println("\n=== CLIENTS ===");
                clients.forEach(c -> System.out.println(c));
            }
        } catch (Exception e) {
            System.err.println("Error listing clients: " + e.getMessage());
            logger.error("Failed to list clients", e);
        }
    }

    @Command(name = "get", description = "Get client details")
    public void get(@Parameters(index = "0", description = "Client ID") Long id) {
        try {
            Client client = clientService.get(id);
            System.out.println(client);
        } catch (Exception e) {
            System.err.println("Error fetching client: " + e.getMessage());
        }
    }

    @Command(name = "delete", description = "Delete a client")
    public void delete(@Parameters(index = "0", description = "Client ID") Long id) {
        try {
            clientService.delete(id);
            System.out.println("Client deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting client: " + e.getMessage());
        }
    }

    @Command(name = "edit", description = "Edit a client")
    public void edit(
            @Parameters(index = "0", description = "Client ID") Long id,
            @Option(names = {"-n", "--name"}) String name,
            @Option(names = {"-p", "--phone"}) String phone,
            @Option(names = {"-e", "--email"}) String email) {
        try {
            Client client = clientService.get(id);
            if (name != null) client.setName(name);
            if (phone != null) client.setPhone(phone);
            if (email != null) client.setEmail(email);
            clientService.update(client);
            System.out.println("Client updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating client: " + e.getMessage());
        }
    }
}
