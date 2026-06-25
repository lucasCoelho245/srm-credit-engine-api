package br.com.srm.receivable;

import br.com.srm.common.exception.BusinessException;
import br.com.srm.common.exception.ResourceNotFoundException;
import br.com.srm.currency.Currency;
import br.com.srm.currency.CurrencyRepository;
import br.com.srm.currency.CurrencyService;
import br.com.srm.currency.ExchangeRate;
import br.com.srm.currency.ExchangeRateRepository;
import br.com.srm.receivable.dto.LiquidateRequest;
import br.com.srm.receivable.dto.LiquidateResponse;
import br.com.srm.receivable.dto.SimulateRequest;
import br.com.srm.receivable.dto.SimulateResponse;
import br.com.srm.receivable.pricing.ChequeStrategy;
import br.com.srm.receivable.pricing.DuplicataStrategy;
import br.com.srm.receivable.pricing.PricingEngine;
import br.com.srm.receivable.pricing.PricingStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceivableService - testes com repositorios mockados")
class ReceivableServiceTest {

    @Mock private ProductTypeRepository productTypeRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private ExchangeRateRepository exchangeRateRepository;

    private ReceivableService receivableService;

    private UUID productTypeId;
    private UUID brlId;
    private UUID usdId;
    private ProductType productType;
    private Currency brl;
    private Currency usd;

    @BeforeEach
    void setUp() throws Exception {
        productTypeId = UUID.randomUUID();
        brlId = UUID.randomUUID();
        usdId = UUID.randomUUID();

        productType = createProductType(productTypeId, "Duplicata Mercantil", new BigDecimal("0.0150"));
        brl = createCurrency(brlId, "BRL", "Real Brasileiro");
        usd = createCurrency(usdId, "USD", "Dolar Americano");

        CurrencyService currencyService = new CurrencyService(currencyRepository, exchangeRateRepository);
        receivableService = new ReceivableService(
                new PricingEngine(),
                new PricingStrategyFactory(List.of(new DuplicataStrategy(), new ChequeStrategy())),
                productTypeRepository,
                transactionRepository,
                currencyRepository,
                currencyService
        );
    }

    @Test
    @DisplayName("Simulacao BRL->BRL retorna VP calculado sem taxa de cambio")
    void shouldSimulateInBrlWithoutExchangeRate() {
        when(productTypeRepository.findById(productTypeId)).thenReturn(Optional.of(productType));
        when(currencyRepository.findById(brlId)).thenReturn(Optional.of(brl));

        SimulateRequest request = new SimulateRequest(
                "Petrobras S.A.", new BigDecimal("10000"), new BigDecimal("0.01"),
                3, productTypeId, brlId, brlId);

        SimulateResponse response = receivableService.simulate(request);

        assertThat(response.presentValue()).isEqualByComparingTo("9285.9941");
        assertThat(response.discount()).isEqualByComparingTo("714.0059");
        assertThat(response.exchangeRateUsed()).isNull();
        verify(exchangeRateRepository, never())
                .findFirstByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        any(), any(), any());
    }

    @Test
    @DisplayName("Lanca ResourceNotFoundException quando tipo de produto nao existe")
    void shouldThrowWhenProductTypeNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(productTypeRepository.findById(unknownId)).thenReturn(Optional.empty());

        SimulateRequest request = new SimulateRequest(
                "Empresa X", new BigDecimal("1000"), new BigDecimal("0.01"),
                3, unknownId, brlId, brlId);

        assertThatThrownBy(() -> receivableService.simulate(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Lanca BusinessException quando taxa de cambio nao existe para cross-currency")
    void shouldThrowWhenExchangeRateNotFound() {
        when(productTypeRepository.findById(productTypeId)).thenReturn(Optional.of(productType));
        when(currencyRepository.findById(brlId)).thenReturn(Optional.of(brl));
        when(currencyRepository.findById(usdId)).thenReturn(Optional.of(usd));
        when(exchangeRateRepository
                .findFirstByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        eq("BRL"), eq("USD"), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        SimulateRequest request = new SimulateRequest(
                "Empresa Y", new BigDecimal("10000"), new BigDecimal("0.01"),
                3, productTypeId, brlId, usdId);

        assertThatThrownBy(() -> receivableService.simulate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Taxa de");
    }

    @Test
    @DisplayName("Liquidacao cross-currency usa taxa vigente na data da liquidacao")
    void shouldLiquidateCrossCurrencyWithCurrentExchangeRate() {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusMonths(6);
        ExchangeRate rate = new ExchangeRate(usd, brl, new BigDecimal("5.410000"), today);

        when(productTypeRepository.findById(productTypeId)).thenReturn(Optional.of(productType));
        when(currencyRepository.findById(usdId)).thenReturn(Optional.of(usd));
        when(currencyRepository.findById(brlId)).thenReturn(Optional.of(brl));
        when(exchangeRateRepository
                .findFirstByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        eq("USD"), eq("BRL"), eq(today)))
                .thenReturn(Optional.of(rate));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LiquidateRequest request = new LiquidateRequest(
                "Acme Exportadora", new BigDecimal("1000"), new BigDecimal("0.01"),
                3, productTypeId, usdId, brlId, dueDate);

        LiquidateResponse response = receivableService.liquidate(request);

        assertThat(response.presentValue()).isEqualByComparingTo("928.5994");
        assertThat(response.exchangeRateUsed()).isEqualByComparingTo("5.410000");
        assertThat(response.dueDate()).isEqualTo(dueDate);
    }

    private ProductType createProductType(UUID id, String name, BigDecimal spread) throws Exception {
        ProductType pt = new ProductType();
        setField(pt, "id", id);
        setField(pt, "name", name);
        setField(pt, "spreadMonthly", spread);
        return pt;
    }

    private Currency createCurrency(UUID id, String code, String name) throws Exception {
        Currency c = new Currency();
        setField(c, "id", id);
        setField(c, "code", code);
        setField(c, "name", name);
        return c;
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
