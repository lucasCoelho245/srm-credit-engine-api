package br.com.srm.reports.repository;

import br.com.srm.receivable.domain.Transaction;
import br.com.srm.reports.dto.TransactionPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Repositório de consultas analíticas sobre transações.
 *
 * Usa JPQL (Java Persistence Query Language) em vez de SQL nativo puro para que
 * o Spring consiga mapear o resultado direto para o DTO, sem precisar de
 * conversores manuais de tipo. JPQL também funciona igual com H2 (testes) e
 * PostgreSQL (produção/Docker), evitando divergências de tipo entre os bancos.
 *
 * O padrão de "expressão construtora" (SELECT new Pacote.Classe(...)) instrui o
 * Hibernate a instanciar o record TransactionPageResponse com os campos certos,
 * sem carregar a entidade Transaction completa na memória.
 */
@Repository
public interface ReportRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Lista todas as transações paginadas, já projetadas para o DTO de resposta.
     * Usado pelo endpoint GET /transactions.
     */
    @Query(value = """
            SELECT new br.com.srm.reports.dto.TransactionPageResponse(
                t.id, t.cedente, t.faceValue, t.presentValue, t.discount,
                cp.code, ct.code, pt.name, t.baseRate, t.termMonths,
                t.exchangeRateUsed, t.dueDate, t.liquidatedAt, t.createdAt)
            FROM Transaction t
            JOIN t.paymentCurrency cp
            JOIN t.titleCurrency ct
            JOIN t.productType pt
            ORDER BY t.createdAt DESC
            """,
            countQuery = "SELECT COUNT(t) FROM Transaction t")
    Page<TransactionPageResponse> findAllProjected(Pageable pageable);

    /**
     * Extrato com filtros opcionais de período, cedente e moeda.
     * Cada filtro é ignorado automaticamente quando o parâmetro chega nulo.
     * Usado pelo endpoint GET /reports/extract.
     */
    @Query(value = """
            SELECT new br.com.srm.reports.dto.TransactionPageResponse(
                t.id, t.cedente, t.faceValue, t.presentValue, t.discount,
                cp.code, ct.code, pt.name, t.baseRate, t.termMonths,
                t.exchangeRateUsed, t.dueDate, t.liquidatedAt, t.createdAt)
            FROM Transaction t
            JOIN t.paymentCurrency cp
            JOIN t.titleCurrency ct
            JOIN t.productType pt
            WHERE (:startDate IS NULL OR t.createdAt >= :startDate)
              AND (:endDate IS NULL OR t.createdAt <= :endDate)
              AND (:cedente IS NULL OR LOWER(t.cedente) LIKE LOWER(CONCAT('%', :cedente, '%')))
              AND (:paymentCurrencyCode IS NULL OR cp.code = :paymentCurrencyCode)
            ORDER BY t.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Transaction t
            JOIN t.paymentCurrency cp
            WHERE (:startDate IS NULL OR t.createdAt >= :startDate)
              AND (:endDate IS NULL OR t.createdAt <= :endDate)
              AND (:cedente IS NULL OR LOWER(t.cedente) LIKE LOWER(CONCAT('%', :cedente, '%')))
              AND (:paymentCurrencyCode IS NULL OR cp.code = :paymentCurrencyCode)
            """)
    Page<TransactionPageResponse> findExtract(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("cedente") String cedente,
            @Param("paymentCurrencyCode") String paymentCurrencyCode,
            Pageable pageable);
}
