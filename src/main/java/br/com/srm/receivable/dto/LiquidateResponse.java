package br.com.srm.receivable.dto;

import br.com.srm.receivable.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LiquidateResponse(
        UUID id,
        String cedente,
        BigDecimal faceValue,
        BigDecimal presentValue,
        BigDecimal discount,
        BigDecimal baseRate,
        Integer termMonths,
        String productTypeName,
        String titleCurrencyCode,
        String paymentCurrencyCode,
        BigDecimal exchangeRateUsed,
        LocalDate dueDate,
        Instant liquidatedAt
) {
    public static LiquidateResponse from(Transaction t) {
        return new LiquidateResponse(
                t.getId(),
                t.getCedente(),
                t.getFaceValue(),
                t.getPresentValue(),
                t.getDiscount(),
                t.getBaseRate(),
                t.getTermMonths(),
                t.getProductType().getName(),
                t.getTitleCurrency().getCode(),
                t.getPaymentCurrency().getCode(),
                t.getExchangeRateUsed(),
                t.getDueDate(),
                t.getLiquidatedAt()
        );
    }
}
