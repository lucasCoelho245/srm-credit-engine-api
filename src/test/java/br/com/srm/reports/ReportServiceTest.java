package br.com.srm.reports;

import br.com.srm.reports.dto.TransactionPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
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

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("extract retorna page tipada vinda do repositório")
    void shouldReturnExtractPage() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-25T05:00:00Z");

        TransactionPageResponse response = new TransactionPageResponse(
                id,
                "Smoke Extract SRM",
                new BigDecimal("10000.0000"),
                new BigDecimal("9285.9941"),
                new BigDecimal("714.0059"),
                "BRL", "BRL", "Duplicata Mercantil",
                new BigDecimal("0.0100"),
                3,
                null,
                LocalDate.parse("2026-12-25"),
                now,
                now
        );

        PageRequest pageable = PageRequest.of(0, 5);
        when(reportRepository.findExtract(isNull(), isNull(), eq("Smoke"), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        var page = reportService.extract(null, null, "Smoke", null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().id()).isEqualTo(id);
        assertThat(page.getContent().getFirst().cedente()).isEqualTo("Smoke Extract SRM");
        assertThat(page.getContent().getFirst().presentValue())
                .isEqualByComparingTo("9285.9941");
    }

    @Test
    @DisplayName("listTransactions delega para findAllProjected do repositório")
    void shouldDelegateListTransactions() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(reportRepository.findAllProjected(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var page = reportService.listTransactions(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isZero();
    }
}
