package bg.company.transport.cli;

import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.reporting.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Command(name = "report", description = "Generate reports")
public class ReportCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ReportCommand.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final ReportingService reportingService;

    public ReportCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            this.reportingService = new ReportingService(dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ReportCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Generate reports. Use: report totals|revenue|drivers|driver-revenue");
    }

    @Command(name = "totals", description = "Show total transport count and revenue")
    public void totals(@Option(names = {"--paid-only"}, description = "Only count paid transports") boolean paidOnly) {
        try {
            long count = reportingService.totalTransports();
            BigDecimal revenue = reportingService.totalRevenue(paidOnly);
            System.out.println("\n=== TOTALS REPORT ===");
            System.out.println("Total Transports: " + count);
            System.out.println("Total Revenue: " + revenue + (paidOnly ? " (paid only)" : ""));
        } catch (Exception e) {
            System.err.println("Error generating totals report: " + e.getMessage());
            logger.error("Failed to generate totals report", e);
        }
    }

    @Command(name = "drivers", description = "Show transport count per driver")
    public void drivers() {
        try {
            Map<Long, Long> driverTrips = reportingService.driverTripsCount();
            System.out.println("\n=== DRIVERS REPORT ===");
            System.out.printf("%-15s %-10s%n", "Driver ID", "Trips");
            System.out.println("---------------------------");
            driverTrips.forEach((driverId, trips) ->
                System.out.printf("%-15d %-10d%n", driverId, trips)
            );
        } catch (Exception e) {
            System.err.println("Error generating drivers report: " + e.getMessage());
            logger.error("Failed to generate drivers report", e);
        }
    }

    @Command(name = "revenue", description = "Show revenue for a period")
    public void revenue(
            @Option(names = {"--from"}, description = "Start date (YYYY-MM-DD)", required = true) String from,
            @Option(names = {"--to"}, description = "End date (YYYY-MM-DD)", required = true) String to) {
        try {
            LocalDate fromDate = LocalDate.parse(from, DATE_FMT);
            LocalDate toDate = LocalDate.parse(to, DATE_FMT);
            BigDecimal revenue = reportingService.revenueForPeriod(fromDate, toDate);
            System.out.println("\n=== REVENUE REPORT ===");
            System.out.println("Period: " + from + " to " + to);
            System.out.println("Revenue: " + revenue);
        } catch (Exception e) {
            System.err.println("Error generating revenue report: " + e.getMessage());
            logger.error("Failed to generate revenue report", e);
        }
    }

    @Command(name = "driver-revenue", description = "Show revenue per driver for a period")
    public void driverRevenue(
            @Option(names = {"--from"}, description = "Start date (YYYY-MM-DD)", required = true) String from,
            @Option(names = {"--to"}, description = "End date (YYYY-MM-DD)", required = true) String to) {
        try {
            LocalDate fromDate = LocalDate.parse(from, DATE_FMT);
            LocalDate toDate = LocalDate.parse(to, DATE_FMT);
            Map<Long, BigDecimal> driverRevenue = reportingService.revenuePerDriver(fromDate, toDate);
            System.out.println("\n=== DRIVER REVENUE REPORT ===");
            System.out.println("Period: " + from + " to " + to);
            System.out.printf("%-15s %-15s%n", "Driver ID", "Revenue");
            System.out.println("-------------------------------");
            driverRevenue.forEach((driverId, rev) ->
                System.out.printf("%-15d %-15s%n", driverId, rev)
            );
        } catch (Exception e) {
            System.err.println("Error generating driver revenue report: " + e.getMessage());
            logger.error("Failed to generate driver revenue report", e);
        }
    }
}
