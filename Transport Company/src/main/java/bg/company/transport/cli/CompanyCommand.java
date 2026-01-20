package bg.company.transport.cli;

import bg.company.transport.dao.JdbcTransportCompanyDao;
import bg.company.transport.domain.TransportCompany;
import bg.company.transport.service.CompanyService;
import bg.company.transport.config.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "company", description = "Manage transport companies")
public class CompanyCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CompanyCommand.class);
    
    private final CompanyService companyService;

    public CompanyCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var dao = new JdbcTransportCompanyDao(dataSource);
            this.companyService = new CompanyService(dao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize CompanyCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Company management. Use: company add|edit|delete|get|list [options]");
    }

    @Command(name = "add", description = "Add a new company")
    public void add(
            @Parameters(index = "0", description = "Company name") String name,
            @Parameters(index = "1", description = "Company address") String address) {
        try {
            TransportCompany company = new TransportCompany(name, address);
            TransportCompany created = companyService.create(company);
            System.out.println("Company created successfully with ID: " + created.getId());
        } catch (Exception e) {
            System.err.println("Error creating company: " + e.getMessage());
            logger.error("Failed to create company", e);
        }
    }

    @Command(name = "edit", description = "Edit a company")
    public void edit(
            @Parameters(index = "0", description = "Company ID") Long id,
            @Option(names = {"-n", "--name"}, description = "New name") String name,
            @Option(names = {"-a", "--address"}, description = "New address") String address) {
        try {
            TransportCompany company = companyService.get(id);
            if (name != null) company.setName(name);
            if (address != null) company.setAddress(address);
            companyService.update(company);
            System.out.println("Company updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating company: " + e.getMessage());
            logger.error("Failed to update company", e);
        }
    }

    @Command(name = "delete", description = "Delete a company")
    public void delete(@Parameters(index = "0", description = "Company ID") Long id) {
        try {
            companyService.delete(id);
            System.out.println("Company deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting company: " + e.getMessage());
            logger.error("Failed to delete company", e);
        }
    }

    @Command(name = "get", description = "Get company details")
    public void get(@Parameters(index = "0", description = "Company ID") Long id) {
        try {
            TransportCompany company = companyService.get(id);
            System.out.println(company);
        } catch (Exception e) {
            System.err.println("Error fetching company: " + e.getMessage());
            logger.error("Failed to fetch company", e);
        }
    }

    @Command(name = "list", description = "List all companies")
    public void list(
            @Option(names = {"--sort"}, description = "Sort by: name|revenue") String sort) {
        try {
            List<TransportCompany> companies = companyService.list(sort != null ? sort : "name");
            if (companies.isEmpty()) {
                System.out.println("No companies found");
            } else {
                System.out.println("\n=== COMPANIES ===");
                companies.forEach(c -> System.out.println(c));
            }
        } catch (Exception e) {
            System.err.println("Error listing companies: " + e.getMessage());
            logger.error("Failed to list companies", e);
        }
    }
}
