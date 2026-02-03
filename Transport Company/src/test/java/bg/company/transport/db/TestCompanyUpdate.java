package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.service.CompanyService;

import javax.sql.DataSource;

public class TestCompanyUpdate {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));

            // Specify company ID for full control
            Long companyId = 1L;
            System.out.println("✓ Using company ID: " + companyId);
            
            // Get and update the company
            TransportCompany company = companyService.get(companyId);
            company.setName("Updated Company Name");
            company.setAddress("Updated Address, City");
            companyService.update(company);
            
            // Verify changes
            TransportCompany updated = companyService.get(companyId);
            System.out.println("✓ Company updated successfully!");
            System.out.println("New name: " + updated.getName());
            System.out.println("New address: " + updated.getAddress());
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
