package br.com.srm.reports.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
