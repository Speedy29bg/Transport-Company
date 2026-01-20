package bg.company.transport.io;

import bg.company.transport.domain.Transport;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransportCsvExporter {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public void export(List<Transport> transports, Writer writer) throws IOException {
        writer.write("companyId,clientId,origin,destination,departAt,arriveAt,cargoKind,cargoWeightKg,passengerCount,price,driverId,vehicleId,paymentStatus\n");
        for (Transport t : transports) {
            writer.write(String.join(",",
                    String.valueOf(t.getCompanyId()),
                    String.valueOf(t.getClientId()),
                    escape(t.getOrigin()),
                    escape(t.getDestination()),
                    t.getDepartAt().format(FMT),
                    t.getArriveAt().format(FMT),
                    t.getCargoKind().name(),
                    t.getCargoWeightKg() == null ? "" : String.valueOf(t.getCargoWeightKg()),
                    t.getPassengerCount() == null ? "" : String.valueOf(t.getPassengerCount()),
                    t.getPrice().toPlainString(),
                    String.valueOf(t.getDriverId()),
                    String.valueOf(t.getVehicleId()),
                    t.getPaymentStatus().name()
            ));
            writer.write("\n");
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}
