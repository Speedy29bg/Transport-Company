package bg.company.transport.io;

import bg.company.transport.domain.CargoKind;
import bg.company.transport.domain.PaymentStatus;
import bg.company.transport.domain.Transport;
import bg.company.transport.service.TransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class TransportCsvImporter {
    private static final Logger log = LoggerFactory.getLogger(TransportCsvImporter.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public ImportResult importFile(Reader reader, TransportService transportService) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String header = br.readLine();
            if (header == null) {
                return new ImportResult(0, 0);
            }
            AtomicInteger ok = new AtomicInteger();
            AtomicInteger failed = new AtomicInteger();
            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                row++;
                String[] parts = parseCsvLine(line);
                if (parts.length < 13) {
                    log.warn("Row {} invalid column count", row);
                    failed.incrementAndGet();
                    continue;
                }
                try {
                    Transport t = new Transport();
                    t.setCompanyId(Long.parseLong(parts[0]));
                    t.setClientId(Long.parseLong(parts[1]));
                    t.setOrigin(parts[2]);
                    t.setDestination(parts[3]);
                    t.setDepartAt(OffsetDateTime.parse(parts[4], FMT));
                    t.setArriveAt(OffsetDateTime.parse(parts[5], FMT));
                    t.setCargoKind(CargoKind.valueOf(parts[6]));
                    t.setCargoWeightKg(parts[7].isEmpty() ? null : Integer.parseInt(parts[7]));
                    t.setPassengerCount(parts[8].isEmpty() ? null : Integer.parseInt(parts[8]));
                    t.setPrice(new BigDecimal(parts[9]));
                    t.setDriverId(Long.parseLong(parts[10]));
                    t.setVehicleId(Long.parseLong(parts[11]));
                    t.setPaymentStatus(PaymentStatus.valueOf(parts[12]));
                    transportService.create(t);
                    ok.incrementAndGet();
                } catch (Exception ex) {
                    log.warn("Failed to import row {}: {}", row, ex.getMessage());
                    failed.incrementAndGet();
                }
            }
            return new ImportResult(ok.get(), failed.get());
        }
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV split that respects quoted values
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        java.util.List<String> tokens = new java.util.ArrayList<>();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}
