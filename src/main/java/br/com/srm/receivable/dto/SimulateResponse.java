package br.com.srm.receivable.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resultado da simulação de deságio")
public record SimulateResponse(
        @Schema(description = "Valor de face (input)", example = "10000.0000")
        BigDecimal faceValue,

        @Schema(description = "Valor presente calculado", example = "9285.9941")
        BigDecimal presentValue,

        @Schema(description = "Deságio aplicado (VF - VP)", example = "714.0059")
        BigDecimal discount,

        @Schema(description = "Percentual de deságio sobre o VF", example = "7.15")
        BigDecimal discountPercent,

        @Schema(description = "Valor presente em moeda de pagamento (se cross-currency)")
        BigDecimal presentValuePaymentCurrency,

        @Schema(description = "Taxa de câmbio utilizada (null se mesma moeda)")
        BigDecimal exchangeRateUsed,

        @Schema(description = "Código da moeda do título", example = "BRL")
        String titleCurrencyCode,

        @Schema(description = "Código da moeda de pagamento", example = "USD")
        String paymentCurrencyCode
) {}
