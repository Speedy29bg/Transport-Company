package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcClientDao;
import bg.company.transport.domain.Client;
import bg.company.transport.domain.ClientType;
import bg.company.transport.service.ClientService;

import javax.sql.DataSource;

public class TestClientDelete {
    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            ClientService clientService = new ClientService(new JdbcClientDao(dataSource));
            
            // Use existing client ID to delete (assuming client with ID=2 exists)
            Long clientId = 6L;
            System.out.println("✓ Deleting client ID: " + clientId);
            
            // Delete the client
            clientService.delete(clientId);
            System.out.println("✓ Client deleted successfully!");
            
            // Verify deletion
            try {
                clientService.get(clientId);
                System.err.println("✗ Error: Client still exists!");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ Confirmed: Client does not exist.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
