package br.com.srm.receivable.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia de precificação para Duplicata Mercantil.
 *
 * Duplicata Mercantil é o título de crédito emitido pelo vendedor quando há
 * uma venda a prazo de mercadorias. É considerado de menor risco em relação
 * ao Cheque Pré-datado, por isso tem spread menor: 1,5% ao mês.
 *
 * O @Component faz o Spring registrar esta classe automaticamente no contexto,
 * o que permite a PricingStrategyFactory receber a lista de todas as estratégias
 * via injeção de dependência.
 */
@Component
public class DuplicataStrategy implements PricingStrategy {

    // Spread de 1,5% ao mês conforme regra de negócio do desafio
    private static final BigDecimal SPREAD_MONTHLY = new BigDecimal("0.0150");

    // Nome deve bater exatamente com o campo "name" em product_types no banco
    private static final String NAME = "Duplicata Mercantil";

    @Override
    public BigDecimal getSpreadMonthly() {
        return SPREAD_MONTHLY;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
