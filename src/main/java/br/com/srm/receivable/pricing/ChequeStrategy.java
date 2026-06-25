package br.com.srm.receivable.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ChequeStrategy implements PricingStrategy {

    private static final BigDecimal SPREAD_MONTHLY = new BigDecimal("0.0250");
    private static final String NAME = "Cheque Pre-datado";

    @Override
    public BigDecimal getSpreadMonthly() {
        return SPREAD_MONTHLY;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
