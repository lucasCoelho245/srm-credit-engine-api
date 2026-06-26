package br.com.srm.receivable.pricing;

import java.math.BigDecimal;

/**
 * Resultado imutável do cálculo financeiro retornado pelo PricingEngine.
 *
 * Record é ideal aqui: cria automaticamente construtor, getters, equals, hashCode
 * e toString sem boilerplate. Como o resultado não precisa ser modificado após
 * o cálculo, a imutabilidade do record garante segurança em ambientes concorrentes.
 */
public record PricingResult(BigDecimal presentValue, BigDecimal discount) {}
