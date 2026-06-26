package br.com.srm.reports.service;

import br.com.srm.reports.dto.TransactionPageResponse;
import br.com.srm.reports.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Serviço de relatórios: listagem geral e extrato filtrado de transações.
 *
 * Fica na camada de aplicação — não contém regra de negócio financeira, só
 * coordena a busca dos dados e os devolve ao controller. A projeção para DTO
 * acontece dentro da própria query JPQL no ReportRepository, então este
 * serviço não precisa de nenhuma lógica de mapeamento manual.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    /**
     * Retorna todas as transações paginadas, ordenadas da mais recente para a mais antiga.
     * Sem filtros — usado na tela de Transações.
     */
    @Transactional(readOnly = true)
    public Page<TransactionPageResponse> listTransactions(Pageable pageable) {
        return reportRepository.findAllProjected(pageable);
    }

    /**
     * Retorna o extrato filtrado. Qualquer parâmetro pode ser nulo — quando nulo,
     * o filtro correspondente é ignorado pela query.
     *
     * @param startDate           início do período (inclusivo), pode ser nulo
     * @param endDate             fim do período (inclusivo), pode ser nulo
     * @param cedente             nome parcial do cedente (busca case-insensitive), pode ser nulo
     * @param paymentCurrencyCode código da moeda de pagamento (ex: "BRL"), pode ser nulo
     */
    @Transactional(readOnly = true)
    public Page<TransactionPageResponse> extract(
            Instant startDate, Instant endDate,
            String cedente, String paymentCurrencyCode, Pageable pageable) {
        return reportRepository.findExtract(startDate, endDate, cedente, paymentCurrencyCode, pageable);
    }
}
