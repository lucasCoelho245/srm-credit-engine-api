package br.com.srm.receivable.pricing;

import br.com.srm.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class PricingEngine {

    private static final int MONETARY_SCALE = 4;
    private static final int INTERMEDIATE_SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public PricingResult calculate(BigDecimal faceValue, BigDecimal baseRate,
                                   int termMonths, PricingStrategy strategy) {
        validateInputs(faceValue, baseRate, termMonths);

        // Aqui fica o coração do cálculo: taxa base informada + spread do produto,
        // trazendo o valor de face para valor presente pelo prazo em meses.
        BigDecimal totalRate = baseRate.add(strategy.getSpreadMonthly());
        BigDecimal base = BigDecimal.ONE.add(totalRate);

        // Uso uma escala intermediária maior para não perder precisão antes do
        // arredondamento financeiro final que vai para a resposta e para o banco.
        BigDecimal divisor = base.pow(termMonths, new MathContext(INTERMEDIATE_SCALE + 4, ROUNDING_MODE))
                .setScale(INTERMEDIATE_SCALE, ROUNDING_MODE);

        BigDecimal presentValue = faceValue.divide(divisor, MONETARY_SCALE, ROUNDING_MODE);
        BigDecimal discount = faceValue.subtract(presentValue)
                .setScale(MONETARY_SCALE, ROUNDING_MODE);

        return new PricingResult(presentValue, discount);
    }

    public BigDecimal convertCrossCurrency(BigDecimal presentValue, BigDecimal exchangeRate) {
        if (presentValue == null) {
            throw new BusinessException("Valor presente invalido para conversao cross-currency");
        }
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Taxa de câmbio inválida para conversão cross-currency");
        }
        // A taxa cadastrada representa origem -> destino. Ex.: USD -> BRL com 5.41
        // multiplica o VP em USD para mostrar quanto será pago em BRL.
        return presentValue.multiply(exchangeRate).setScale(MONETARY_SCALE, ROUNDING_MODE);
    }

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
