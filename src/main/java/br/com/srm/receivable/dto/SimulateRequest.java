package br.com.srm.receivable.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Requisição de simulação de deságio (sem persistência)")
public record SimulateRequest(

        @NotBlank(message = "Cedente é obrigatório")
        @Size(max = 200, message = "Cedente deve ter no máximo 200 caracteres")
        @Schema(description = "Nome da empresa cedente", example = "Petrobras S.A.")
        String cedente,

        @NotNull(message = "Valor de face é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor de face deve ser maior que zero")
        @Schema(description = "Valor nominal do título", example = "10000.00")
        BigDecimal faceValue,

        @NotNull(message = "Taxa base é obrigatória")
        @DecimalMin(value = "0.0", message = "Taxa base não pode ser negativa")
        @DecimalMax(value = "9.9999", message = "Taxa base deve ser no máximo 9.9999")
        @Schema(description = "Taxa base mensal (ex: CDI mensal)", example = "0.0100")
        BigDecimal baseRate,

        @NotNull(message = "Prazo em meses é obrigatório")
        @Min(value = 1, message = "Prazo deve ser no mínimo 1 mês")
        @Max(value = 360, message = "Prazo máximo é 360 meses")
        @Schema(description = "Quantidade de meses até o vencimento", example = "3")
        Integer termMonths,

        @NotNull(message = "Tipo de produto é obrigatório")
        @Schema(description = "UUID do tipo de produto (product_types.id)")
        UUID productTypeId,

        @NotNull(message = "Moeda do título é obrigatória")
        @Schema(description = "UUID da moeda do título")
        UUID titleCurrencyId,

        @NotNull(message = "Moeda de pagamento é obrigatória")
        @Schema(description = "UUID da moeda de pagamento")
        UUID paymentCurrencyId
) {}
