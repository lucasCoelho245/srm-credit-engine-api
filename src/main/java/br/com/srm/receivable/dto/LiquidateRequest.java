package br.com.srm.receivable.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Requisição de liquidação — persiste a transação em ACID")
public record LiquidateRequest(

        @NotBlank(message = "Cedente é obrigatório")
        @Size(max = 200)
        String cedente,

        @NotNull
        @DecimalMin(value = "0.01", message = "Valor de face deve ser maior que zero")
        BigDecimal faceValue,

        @NotNull
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "9.9999", message = "Taxa base deve ser no máximo 9.9999")
        BigDecimal baseRate,

        @NotNull
        @Min(1)
        Integer termMonths,

        @NotNull
        UUID productTypeId,

        @NotNull
        UUID titleCurrencyId,

        @NotNull
        UUID paymentCurrencyId,

        @NotNull(message = "Data de vencimento é obrigatória")
        @Future(message = "Data de vencimento deve ser futura")
        LocalDate dueDate
) {}
