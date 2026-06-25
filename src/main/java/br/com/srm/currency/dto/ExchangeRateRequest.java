package br.com.srm.currency.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Requisição para cadastrar taxa de câmbio")
public record ExchangeRateRequest(

        @NotBlank(message = "Código da moeda origem é obrigatório")
        @Size(min = 3, max = 3, message = "Código da moeda deve ter 3 caracteres")
        @Schema(description = "Código da moeda de origem (ex: USD)", example = "USD")
        String fromCurrencyCode,

        @NotBlank(message = "Código da moeda destino é obrigatório")
        @Size(min = 3, max = 3, message = "Código da moeda deve ter 3 caracteres")
        @Schema(description = "Código da moeda de destino (ex: BRL)", example = "BRL")
        String toCurrencyCode,

        @NotNull(message = "Taxa de câmbio é obrigatória")
        @DecimalMin(value = "0.000001", message = "Taxa deve ser maior que zero")
        @Schema(description = "Taxa de câmbio com 6 casas decimais", example = "5.200000")
        BigDecimal rate,

        @NotNull(message = "Data de vigência é obrigatória")
        @Schema(description = "Data de vigência da taxa", example = "2024-06-15")
        LocalDate effectiveDate
) {}
