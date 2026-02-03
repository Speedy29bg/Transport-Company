package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcClientDao;
import bg.company.transport.domain.Client;
import bg.company.transport.domain.ClientType;
import bg.company.transport.service.ClientService;

import javax.sql.DataSource;

public class TestClientAdd {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            ClientService clientService = new ClientService(new JdbcClientDao(dataSource));

            Client client = new Client();
            client.setName("John Smith");
            client.setPhone("+359888123456");
            client.setEmail("john.smith@email.com");
            client.setType(ClientType.PERSON);
            
            Client created = clientService.create(client);
            
            System.out.println("✓ Client created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getName());
            System.out.println("Phone: " + created.getPhone());
            System.out.println("Email: " + created.getEmail());
            System.out.println("Type: " + created.getType());
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
