package br.com.srm.receivable.pricing;

import br.com.srm.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PricingEngine — Testes Unitários do Motor de Cálculo")
class PricingEngineTest {

    private final PricingEngine engine = new PricingEngine();
    private final PricingStrategy duplicata = new DuplicataStrategy();
    private final PricingStrategy cheque = new ChequeStrategy();

    @Test
    @DisplayName("VP correto para Duplicata: VF=10.000, taxaBase=1%, 3 meses -> 9285.9941")
    void shouldCalculatePresentValueForDuplicata() {
        BigDecimal faceValue = new BigDecimal("10000");
        BigDecimal baseRate = new BigDecimal("0.01");

        PricingResult result = engine.calculate(faceValue, baseRate, 3, duplicata);

        assertThat(result.presentValue()).isEqualByComparingTo("9285.9941");
        assertThat(result.discount()).isEqualByComparingTo("714.0059");
        assertThat(result.presentValue().scale()).isEqualTo(4);
    }

    @Test
    @DisplayName("VP correto para Cheque: VF=5.000, taxaBase=0.5%, 6 meses — VP < VF")
    void shouldCalculatePresentValueForCheque() {
        BigDecimal faceValue = new BigDecimal("5000");
        BigDecimal baseRate = new BigDecimal("0.005");

        PricingResult result = engine.calculate(faceValue, baseRate, 6, cheque);

        assertThat(result.presentValue()).isLessThan(faceValue);
        assertThat(result.discount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.presentValue().scale()).isEqualTo(4);
        assertThat(result.presentValue()).isEqualByComparingTo("4187.4213");
    }

    @Test
    @DisplayName("Cross-currency BRL→USD: VP=9285.26, taxa=0.192308 → 1785.6298")
    void shouldConvertCrossCurrency() {
        BigDecimal presentValueBrl = new BigDecimal("9285.26");
        BigDecimal exchangeRate = new BigDecimal("0.192308");

        BigDecimal vpUsd = engine.convertCrossCurrency(presentValueBrl, exchangeRate);

        assertThat(vpUsd).isEqualByComparingTo("1785.6298");
    }

    @Test
    @DisplayName("Arredondamento HALF_EVEN — escala 4 casas decimais garantida")
    void shouldApplyHalfEvenRounding() {
        BigDecimal faceValue = new BigDecimal("12345.67");
        BigDecimal baseRate = new BigDecimal("0.0123");

        PricingResult result = engine.calculate(faceValue, baseRate, 4, duplicata);

        assertThat(result.presentValue().scale()).isEqualTo(4);
        assertThat(result.discount().scale()).isEqualTo(4);
    }

    @Test
    @DisplayName("Valor face negativo deve lançar BusinessException com 'face_value'")
    void shouldRejectNegativeFaceValue() {
        BigDecimal negativeFaceValue = new BigDecimal("-100");

        assertThatThrownBy(() -> engine.calculate(negativeFaceValue, BigDecimal.ZERO, 3, duplicata))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("face_value");
    }

    @Test
    @DisplayName("Valor face zero deve lançar BusinessException")
    void shouldRejectZeroFaceValue() {
        assertThatThrownBy(() -> engine.calculate(BigDecimal.ZERO, BigDecimal.ZERO, 3, duplicata))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("face_value");
    }

    @Test
    @DisplayName("Prazo zero deve lançar BusinessException com 'term_months'")
    void shouldRejectZeroTermMonths() {
        assertThatThrownBy(() -> engine.calculate(new BigDecimal("1000"), BigDecimal.ZERO, 0, duplicata))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("term_months");
    }

    @Test
    @DisplayName("Taxa base negativa deve lançar BusinessException com 'base_rate'")
    void shouldRejectNegativeBaseRate() {
        assertThatThrownBy(() -> engine.calculate(new BigDecimal("1000"), new BigDecimal("-0.01"), 3, duplicata))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("base_rate");
    }

    @Test
    @DisplayName("Taxa de câmbio zero deve lançar BusinessException na conversão")
    void shouldRejectZeroExchangeRateInConversion() {
        assertThatThrownBy(() -> engine.convertCrossCurrency(new BigDecimal("1000"), BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class);
    }
}
