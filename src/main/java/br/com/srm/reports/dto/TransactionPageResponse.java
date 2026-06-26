package br.com.srm.reports.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de projeção de transação para listagem e extrato.
 *
 * Instanciado diretamente pela query JPQL com expressão construtora (SELECT new ...) no
 * ReportRepository — o Hibernate preenche cada campo pelo construtor canônico do record
 * sem carregar a entidade Transaction completa com todos os seus relacionamentos lazy.
 */
public record TransactionPageResponse(
        UUID id,
        String cedente,
        BigDecimal faceValue,
        BigDecimal presentValue,
        BigDecimal discount,
        String paymentCurrencyCode,
        String titleCurrencyCode,
        String productTypeName,
        BigDecimal baseRate,
        Integer termMonths,
        BigDecimal exchangeRateUsed,
        LocalDate dueDate,
        Instant liquidatedAt,
        Instant createdAt
) {}
