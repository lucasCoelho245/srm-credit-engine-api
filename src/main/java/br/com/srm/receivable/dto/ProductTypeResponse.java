package br.com.srm.receivable.dto;

import br.com.srm.receivable.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductTypeResponse(UUID id, String name, BigDecimal spreadMonthly, String description) {
    public static ProductTypeResponse from(ProductType p) {
        return new ProductTypeResponse(p.getId(), p.getName(), p.getSpreadMonthly(), p.getDescription());
    }
}
