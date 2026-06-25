package br.com.srm.receivable.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal getSpreadMonthly();

    String getName();
}
