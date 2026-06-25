package br.com.srm.receivable.pricing;

import java.math.BigDecimal;

public record PricingResult(BigDecimal presentValue, BigDecimal discount) {}
