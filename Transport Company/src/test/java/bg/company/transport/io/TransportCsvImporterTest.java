package bg.company.transport.io;

import bg.company.transport.domain.Transport;
import bg.company.transport.service.TransportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportCsvImporterTest {

    @Mock
    private TransportService transportService;

    @Test
    void importsValidRowsAndCountsFailures() throws Exception {
        String csv = String.join("\n",
                "companyId,clientId,origin,destination,departAt,arriveAt,cargoKind,cargoWeightKg,passengerCount,price,driverId,vehicleId,paymentStatus",
                "1,1,Sofia,Plovdiv,2026-01-20T08:00:00Z,2026-01-20T10:00:00Z,PASSENGERS,,10,100.00,1,1,UNPAID",
                "1,1,Sofia,Plovdiv,INVALID_DATE,2026-01-20T10:00:00Z,PASSENGERS,,10,100.00,1,1,UNPAID"
        );

        when(transportService.create(any(Transport.class))).thenAnswer(inv -> inv.getArgument(0));

        TransportCsvImporter importer = new TransportCsvImporter();
        ImportResult result = importer.importFile(new StringReader(csv), transportService);

        assertEquals(1, result.imported());
        assertEquals(1, result.failed());
        verify(transportService, times(1)).create(any(Transport.class));
    }
}
