package br.com.srm.reports;

import br.com.srm.receivable.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService")
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Mapeia UUID retornado como bytes em SQL nativo no H2")
    void shouldMapNativeSqlUuidReturnedAsBytes() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-25T05:00:00Z");
        Object[] row = {
                uuidToBytes(id),
                "Smoke Extract SRM",
                new BigDecimal("10000.0000"),
                new BigDecimal("9285.9941"),
                new BigDecimal("714.0059"),
                "BRL",
                "BRL",
                "Duplicata Mercantil",
                new BigDecimal("0.0100"),
                3,
                null,
                LocalDate.parse("2026-12-25"),
                now,
                now
        };

        PageRequest pageable = PageRequest.of(0, 5);
        when(reportRepository.findExtract(isNull(), isNull(), eq("Smoke"), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.<Object[]>of(row), pageable, 1));

        var page = new ReportService(reportRepository, transactionRepository)
                .extract(null, null, "Smoke", null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().id()).isEqualTo(id);
        assertThat(page.getContent().getFirst().cedente()).isEqualTo("Smoke Extract SRM");
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}
