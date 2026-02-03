package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.service.CompanyService;

import javax.sql.DataSource;

public class TestCompanyAdd {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));

            TransportCompany company = new TransportCompany("Express Logistics Ltd", "Sofia, Vitosha Blvd 1");
            TransportCompany created = companyService.create(company);
            
            System.out.println("✓ Company created successfully!");
            System.out.println("ID: " + created.getId());
            System.out.println("Name: " + created.getName());
            System.out.println("Address: " + created.getAddress());
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
