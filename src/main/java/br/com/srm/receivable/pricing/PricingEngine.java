package br.com.srm.receivable.pricing;

import br.com.srm.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Motor de precificação: implementa o cálculo de Valor Presente e Deságio.
 *
 * Fórmula aplicada:
 *   VP = VF / (1 + taxaBase + spread) ^ prazo
 *   Deságio = VF - VP
 *
 * Onde:
 *   VF        = Valor de Face (valor nominal do recebível)
 *   taxaBase  = taxa informada pelo operador (ex: 0.01 = 1% a.m.)
 *   spread    = risco do tipo de produto (definido pela PricingStrategy)
 *   prazo     = número de meses até o vencimento
 *   VP        = Valor Presente (quanto o fundo paga hoje pelo recebível)
 *   Deságio   = quanto o cedente "perde" ao antecipar o recebível (lucro do fundo)
 *
 * Usamos BigDecimal em vez de double/float porque operações de ponto flutuante
 * acumulam erros de arredondamento em cálculos financeiros — BigDecimal é exato.
 */
@Component
public class PricingEngine {

    // Escala final: 4 casas decimais para valores monetários (padrão financeiro)
    private static final int MONETARY_SCALE = 4;

    // Escala intermediária maior evita perda de precisão durante as operações antes do arredondamento final
    private static final int INTERMEDIATE_SCALE = 10;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Executa o cálculo financeiro e retorna VP e Deságio.
     *
     * @param faceValue  valor de face do recebível (deve ser > 0)
     * @param baseRate   taxa base mensal informada pelo operador (ex: 0.01 = 1% a.m.)
     * @param termMonths prazo em meses até o vencimento (deve ser > 0)
     * @param strategy   estratégia que define o spread do tipo de produto
     */
    public PricingResult calculate(BigDecimal faceValue, BigDecimal baseRate,
                                   int termMonths, PricingStrategy strategy) {
        validateInputs(faceValue, baseRate, termMonths);

        // Taxa total = taxa base do operador + spread de risco do produto
        BigDecimal totalRate = baseRate.add(strategy.getSpreadMonthly());
        BigDecimal base = BigDecimal.ONE.add(totalRate);

        // Elevamos a base ao prazo com escala intermediária para não perder precisão
        // antes do arredondamento financeiro final que vai para o banco e para a resposta
        BigDecimal divisor = base.pow(termMonths, new MathContext(INTERMEDIATE_SCALE + 4, ROUNDING_MODE))
                .setScale(INTERMEDIATE_SCALE, ROUNDING_MODE);

        BigDecimal presentValue = faceValue.divide(divisor, MONETARY_SCALE, ROUNDING_MODE);
        BigDecimal discount = faceValue.subtract(presentValue)
                .setScale(MONETARY_SCALE, ROUNDING_MODE);

        return new PricingResult(presentValue, discount);
    }

    /**
     * Converte o Valor Presente para outra moeda quando o título e o pagamento
     * são em moedas diferentes (operação cross-currency).
     *
     * A taxa cadastrada representa origem → destino. Ex.: USD → BRL com 5.41
     * significa que 1 USD equivale a 5.41 BRL. Multiplica o VP na moeda de origem
     * para obter o valor na moeda de pagamento.
     */
    public BigDecimal convertCrossCurrency(BigDecimal presentValue, BigDecimal exchangeRate) {
        if (presentValue == null) {
            throw new BusinessException("Valor presente invalido para conversao cross-currency");
        }
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Taxa de câmbio inválida para conversão cross-currency");
        }
        return presentValue.multiply(exchangeRate).setScale(MONETARY_SCALE, ROUNDING_MODE);
    }

    /** Garante que os dados de entrada fazem sentido financeiramente antes de calcular. */
    private void validateInputs(BigDecimal faceValue, BigDecimal baseRate, int termMonths) {
        if (faceValue == null || faceValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("face_value deve ser maior que zero");
        }
        if (baseRate == null || baseRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("base_rate não pode ser negativo");
        }
        if (termMonths <= 0) {
            throw new BusinessException("term_months deve ser maior que zero");
        }
    }
}
