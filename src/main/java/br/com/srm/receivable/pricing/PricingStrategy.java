package br.com.srm.receivable.pricing;

import java.math.BigDecimal;

/**
 * Contrato do padrão Strategy para tipos de recebível.
 *
 * Cada tipo de ativo (Duplicata Mercantil, Cheque Pré-datado, etc.) tem um risco
 * diferente, representado pelo spread mensal. Em vez de colocar um if/switch no
 * motor de cálculo para cada tipo, usamos o padrão Strategy: cada tipo de produto
 * é uma classe separada que implementa esta interface. O motor de cálculo só
 * conhece a interface, nunca o tipo concreto — o que torna fácil adicionar novos
 * tipos de recebível sem mexer no cálculo.
 */
public interface PricingStrategy {

    /**
     * Retorna o spread de risco mensal do produto (ex: 0.0150 = 1,5% a.m.).
     * É somado à taxa base informada pelo operador antes de calcular o deságio.
     */
    BigDecimal getSpreadMonthly();

    /**
     * Nome do produto, usado como chave para localizar a estratégia correta
     * na PricingStrategyFactory. Deve bater com o nome cadastrado em product_types.
     */
    String getName();
}
