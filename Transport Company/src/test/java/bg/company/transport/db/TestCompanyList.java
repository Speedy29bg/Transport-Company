package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.service.CompanyService;

import javax.sql.DataSource;
import java.util.List;

public class TestCompanyList {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));

            List<TransportCompany> companies = companyService.list("name");
            
            System.out.println("✓ Found " + companies.size() + " companies:");
            System.out.println("═══════════════════════════════════════════════");
            for (TransportCompany company : companies) {
                System.out.println("ID: " + company.getId() + " | " + company.getName() + " | " + company.getAddress());
            }
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
