package br.com.srm.receivable.pricing;

import br.com.srm.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PricingStrategyFactory — Testes Unitários do Factory Pattern")
class PricingStrategyFactoryTest {

    private PricingStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PricingStrategyFactory(List.of(
                new DuplicataStrategy(),
                new ChequeStrategy()
        ));
    }

    @Test
    @DisplayName("Factory resolve corretamente 'Duplicata Mercantil'")
    void shouldResolveDuplicataStrategy() {
        PricingStrategy strategy = factory.resolve("Duplicata Mercantil");

        assertThat(strategy).isInstanceOf(DuplicataStrategy.class);
        assertThat(strategy.getName()).isEqualTo("Duplicata Mercantil");
        assertThat(strategy.getSpreadMonthly()).isEqualByComparingTo("0.0150");
    }

    @Test
    @DisplayName("Factory resolve corretamente 'Cheque Pre-datado'")
    void shouldResolveChequeStrategy() {
        PricingStrategy strategy = factory.resolve("Cheque Pre-datado");

        assertThat(strategy).isInstanceOf(ChequeStrategy.class);
        assertThat(strategy.getName()).isEqualTo("Cheque Pre-datado");
        assertThat(strategy.getSpreadMonthly()).isEqualByComparingTo("0.0250");
    }

    @Test
    @DisplayName("Factory lança BusinessException para tipo de produto inexistente")
    void shouldThrowForUnknownStrategy() {
        assertThatThrownBy(() -> factory.resolve("Contrato Inexistente"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Contrato Inexistente");
    }

    @Test
    @DisplayName("Spread de Duplicata deve ser menor que spread de Cheque (risco)")
    void duplicataSpreadShouldBeLowerThanCheque() {
        PricingStrategy duplicata = factory.resolve("Duplicata Mercantil");
        PricingStrategy cheque = factory.resolve("Cheque Pre-datado");

        assertThat(duplicata.getSpreadMonthly()).isLessThan(cheque.getSpreadMonthly());
    }
}
