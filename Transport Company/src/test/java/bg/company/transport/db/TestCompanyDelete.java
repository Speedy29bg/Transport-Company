package bg.company.transport.db;

import bg.company.transport.config.ConfigLoader;
import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.service.CompanyService;

import javax.sql.DataSource;

public class TestCompanyDelete {

    public static void main(String[] args) {
        try {
            ConfigLoader.loadDatabaseProperties();
            DataSource dataSource = DataSourceFactory.createDataSource();
            CompanyService companyService = new CompanyService(new JdbcTransportCompanyDao(dataSource));

            // Specify company ID for full control
            Long companyId = 2L;
            System.out.println("✓ Deleting company ID: " + companyId);
            
            // Delete the company
            companyService.delete(companyId);
            System.out.println("✓ Company deleted successfully!");
            
            // Verify deletion
            try {
                companyService.get(companyId);
                System.err.println("✗ Error: Company still exists!");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ Confirmed: Company does not exist.");
            }
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
