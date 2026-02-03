package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcClientDao;
import bg.company.transport.domain.Client;
import bg.company.transport.domain.ClientType;
import bg.company.transport.service.ClientService;

import javax.sql.DataSource;

public class TestClientUpdate {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            ClientService clientService = new ClientService(new JdbcClientDao(dataSource));
            
            // Use existing client ID (assuming client with ID=1 exists)
            Long clientId = 1L;
            System.out.println("✓ Using client ID: " + clientId);
            
            // Get and update the client data
            Client client = clientService.get(clientId);
            client.setName("Updated Client Name");
            client.setPhone("+359888999888");
            client.setEmail("updated.client@newemail.com");
            clientService.update(client);
            
            // Verify changes
            Client updated = clientService.get(clientId);
            System.out.println("✓ Client updated successfully!");
            System.out.println("New name: " + updated.getName());
            System.out.println("New phone: " + updated.getPhone());
            System.out.println("New email: " + updated.getEmail());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
