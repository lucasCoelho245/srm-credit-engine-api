package br.com.srm.receivable.dto;

import br.com.srm.receivable.domain.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

/** DTO de resposta para tipos de recebível — expõe o spread mensal para que o frontend exiba o preview do deságio. */
public record ProductTypeResponse(UUID id, String name, BigDecimal spreadMonthly, String description) {
    public static ProductTypeResponse from(ProductType p) {
        return new ProductTypeResponse(p.getId(), p.getName(), p.getSpreadMonthly(), p.getDescription());
    }
}
