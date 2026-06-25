package br.com.srm.receivable;

import br.com.srm.common.exception.ResourceNotFoundException;
import br.com.srm.currency.Currency;
import br.com.srm.currency.CurrencyRepository;
import br.com.srm.currency.CurrencyService;
import br.com.srm.currency.ExchangeRate;
import br.com.srm.receivable.dto.*;
import br.com.srm.receivable.pricing.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceivableService {

    private final PricingEngine pricingEngine;
    private final PricingStrategyFactory strategyFactory;
    private final ProductTypeRepository productTypeRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;

    @Transactional(readOnly = true)
    public List<ProductTypeResponse> listProductTypes() {
        return productTypeRepository.findAll()
                .stream()
                .map(ProductTypeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    // Simular é só preview: calcula e devolve para o front, mas não grava transação no banco.
    public SimulateResponse simulate(SimulateRequest request) {
        ProductType productType = findProductType(request.productTypeId());
        Currency titleCurrency = findCurrency(request.titleCurrencyId());
        Currency paymentCurrency = findCurrency(request.paymentCurrencyId());

        // A estratégia define o spread conforme o tipo de recebível escolhido pelo operador.
        PricingStrategy strategy = strategyFactory.resolve(productType.getName());
        PricingResult result = pricingEngine.calculate(
                request.faceValue(), request.baseRate(), request.termMonths(), strategy);

        BigDecimal discountPercent = result.discount()
                .divide(request.faceValue(), 4, RoundingMode.HALF_EVEN)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal presentValuePayment = null;
        BigDecimal exchangeRateUsed = null;

        boolean isCrossCurrency = !titleCurrency.getCode().equals(paymentCurrency.getCode());
        if (isCrossCurrency) {
            // Se as moedas forem diferentes, uso a taxa vigente de hoje, que é o momento da operação.
            ExchangeRate rate = currencyService.findRateForDate(
                    titleCurrency.getCode(), paymentCurrency.getCode(), LocalDate.now());
            exchangeRateUsed = rate.getRate();
            presentValuePayment = pricingEngine.convertCrossCurrency(
                    result.presentValue(), exchangeRateUsed);
        }

        return new SimulateResponse(
                request.faceValue(),
                result.presentValue(),
                result.discount(),
                discountPercent,
                presentValuePayment,
                exchangeRateUsed,
                titleCurrency.getCode(),
                paymentCurrency.getCode()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    // Liquidar é o fluxo "valendo": calcula igual à simulação, mas persiste a operação.
    public LiquidateResponse liquidate(LiquidateRequest request) {
        ProductType productType = findProductType(request.productTypeId());
        Currency titleCurrency = findCurrency(request.titleCurrencyId());
        Currency paymentCurrency = findCurrency(request.paymentCurrencyId());

        PricingStrategy strategy = strategyFactory.resolve(productType.getName());
        PricingResult result = pricingEngine.calculate(
                request.faceValue(), request.baseRate(), request.termMonths(), strategy);

        BigDecimal exchangeRateUsed = null;
        boolean isCrossCurrency = !titleCurrency.getCode().equals(paymentCurrency.getCode());
        if (isCrossCurrency) {
            // Guardo a taxa usada para o extrato mostrar exatamente como aquela liquidação foi feita.
            ExchangeRate rate = currencyService.findRateForDate(
                    titleCurrency.getCode(), paymentCurrency.getCode(), LocalDate.now());
            exchangeRateUsed = rate.getRate();
        }

        // Depois do cálculo, monto a entidade que vira histórico da liquidação.
        Transaction transaction = Transaction.create(
                request.cedente(),
                request.faceValue(),
                result.presentValue(),
                result.discount(),
                request.baseRate(),
                request.termMonths(),
                productType,
                titleCurrency,
                paymentCurrency,
                exchangeRateUsed,
                request.dueDate()
        );

        Transaction saved = transactionRepository.save(transaction);

        // A resposta já leva o id e o cedente para o front abrir a listagem no item recém-salvo.
        return new LiquidateResponse(
                saved.getId(),
                saved.getCedente(),
                saved.getFaceValue(),
                saved.getPresentValue(),
                saved.getDiscount(),
                saved.getBaseRate(),
                saved.getTermMonths(),
                productType.getName(),
                titleCurrency.getCode(),
                paymentCurrency.getCode(),
                saved.getExchangeRateUsed(),
                saved.getDueDate(),
                saved.getLiquidatedAt()
        );
    }

    private ProductType findProductType(UUID id) {
        return productTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de produto não encontrado: " + id));
    }

    private Currency findCurrency(UUID id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Moeda não encontrada: " + id));
    }
}
