package br.com.srm.receivable.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia de precificação para Cheque Pré-datado.
 *
 * Cheque Pré-datado tem risco maior que a Duplicata Mercantil porque depende
 * de um instrumento de pagamento (cheque) que pode ser devolvido por insuficiência
 * de fundos ou outros motivos. Por isso, o spread é mais alto: 2,5% ao mês.
 * O deságio maior compensa o risco adicional que o fundo assume ao adquirir esse ativo.
 */
@Component
public class ChequeStrategy implements PricingStrategy {

    // Spread de 2,5% ao mês — maior que a Duplicata por ser ativo de risco mais elevado
    private static final BigDecimal SPREAD_MONTHLY = new BigDecimal("0.0250");

    // Nome deve bater exatamente com o campo "name" em product_types no banco
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
