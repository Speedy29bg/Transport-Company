package bg.company.transport.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(
    name = "transport",
    description = "Transport Company Management System",
    version = "1.0.0",
    subcommands = {
        HelpCommand.class,
        CompanyCommand.class,
        ClientCommand.class,
        EmployeeCommand.class,
        VehicleCommand.class,
        TransportSubCommand.class,
        PaymentCommand.class,
        ExportCommand.class,
        ImportCommand.class,
        ReportCommand.class
    },
    mixinStandardHelpOptions = true
)
public class TransportCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Transport Company Management System");
        System.out.println("Type 'transport help' for usage information");
    }
}
