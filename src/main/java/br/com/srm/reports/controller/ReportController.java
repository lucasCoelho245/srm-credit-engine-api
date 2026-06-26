package br.com.srm.reports.controller;

import br.com.srm.reports.dto.TransactionPageResponse;
import br.com.srm.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Controller de relatórios: listagem geral e extrato filtrado de transações.
 *
 * Todos os parâmetros do extrato são opcionais — o front pode enviar qualquer
 * combinação de filtros. O @PageableDefault configura paginação padrão para
 * não retornar todos os registros quando o cliente não informar tamanho de página.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Listagem paginada e extrato de transações")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/transactions")
    @Operation(summary = "Lista paginada de transações. Server-side pagination.")
    public Page<TransactionPageResponse> listTransactions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return reportService.listTransactions(pageable);
    }

    @GetMapping("/reports/extract")
    @Operation(summary = "Extrato filtrado por período, cedente e moeda. Filtros opcionais.")
    public Page<TransactionPageResponse> extract(
            @Parameter(description = "Data início (ISO-8601)", example = "2024-01-01T00:00:00Z")
            @RequestParam(required = false) Instant startDate,

            @Parameter(description = "Data fim (ISO-8601)", example = "2024-12-31T23:59:59Z")
            @RequestParam(required = false) Instant endDate,

            @Parameter(description = "Nome parcial do cedente (case-insensitive)")
            @RequestParam(required = false) String cedente,

            @Parameter(description = "Código da moeda de pagamento", example = "BRL")
            @RequestParam(required = false) String paymentCurrencyCode,

            @PageableDefault(size = 20) Pageable pageable) {

        return reportService.extract(startDate, endDate, cedente, paymentCurrencyCode, pageable);
    }
}
