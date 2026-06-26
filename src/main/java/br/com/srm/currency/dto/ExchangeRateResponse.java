package br.com.srm.currency.dto;

import br.com.srm.currency.domain.ExchangeRate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Taxa de câmbio cadastrada")
public record ExchangeRateResponse(
        UUID id,
        String fromCurrencyCode,
        String toCurrencyCode,
        BigDecimal rate,
        LocalDate effectiveDate
) {
    public static ExchangeRateResponse from(ExchangeRate e) {
        return new ExchangeRateResponse(
                e.getId(),
                e.getFromCurrency().getCode(),
                e.getToCurrency().getCode(),
                e.getRate(),
                e.getEffectiveDate()
        );
    }
}
