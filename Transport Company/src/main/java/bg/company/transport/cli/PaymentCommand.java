package bg.company.transport.cli;

import bg.company.transport.config.DataSourceFactory;
import bg.company.transport.dao.JdbcEmployeeDao;
import bg.company.transport.dao.JdbcTransportDao;
import bg.company.transport.dao.JdbcVehicleDao;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.service.TransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "payment", description = "Manage payments")
public class PaymentCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(PaymentCommand.class);
    private final TransportService transportService;

    public PaymentCommand() {
        try {
            var dataSource = DataSourceFactory.createDataSource();
            var transportDao = new JdbcTransportDao(dataSource);
            var employeeDao = new JdbcEmployeeDao(dataSource);
            var vehicleDao = new JdbcVehicleDao(dataSource);
            this.transportService = new TransportService(transportDao, employeeDao, vehicleDao);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PaymentCommand", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Payment management. Use: payment set <transportId> <PAID|UNPAID>");
    }

    @Command(name = "set", description = "Set payment status for a transport")
    public void set(
            @Parameters(index = "0", description = "Transport ID") Long transportId,
            @Parameters(index = "1", description = "Payment status: PAID|UNPAID") String status) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            transportService.setPaymentStatus(transportId, paymentStatus);
            System.out.println("Payment status updated to: " + paymentStatus);
        } catch (Exception e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            logger.error("Failed to update payment status", e);
        }
    }
}
