package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcClientDao;
import bg.company.transport.domain.Client;
import bg.company.transport.service.ClientService;

import javax.sql.DataSource;
import java.util.List;

public class TestClientList {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            ClientService clientService = new ClientService(new JdbcClientDao(dataSource));

            List<Client> clients = clientService.list();
            
            System.out.println("✓ Found " + clients.size() + " clients:");
            System.out.println("═══════════════════════════════════════════════");
            for (Client client : clients) {
                System.out.println("ID: " + client.getId() + " | " + client.getName() + 
                                 " | " + client.getPhone() + " | " + client.getType());
            }
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
