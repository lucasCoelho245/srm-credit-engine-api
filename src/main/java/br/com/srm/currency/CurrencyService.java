package br.com.srm.currency;

import br.com.srm.common.exception.BusinessException;
import br.com.srm.common.exception.ResourceNotFoundException;
import br.com.srm.currency.dto.CurrencyResponse;
import br.com.srm.currency.dto.ExchangeRateRequest;
import br.com.srm.currency.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional(readOnly = true)
    public List<CurrencyResponse> listAll() {
        return currencyRepository.findAll()
                .stream()
                .map(CurrencyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> listExchangeRates() {
        return exchangeRateRepository.findAll()
                .stream()
                .map(ExchangeRateResponse::from)
                .toList();
    }

    @Transactional
    public ExchangeRateResponse saveExchangeRate(ExchangeRateRequest request) {
        Currency fromCurrency = currencyRepository.findByCode(request.fromCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Moeda não encontrada: " + request.fromCurrencyCode()));

        Currency toCurrency = currencyRepository.findByCode(request.toCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Moeda não encontrada: " + request.toCurrencyCode()));

        if (fromCurrency.getId().equals(toCurrency.getId())) {
            throw new BusinessException("Moeda origem e destino não podem ser iguais");
        }

        ExchangeRate exchangeRate = exchangeRateRepository
                .findByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDate(
                        fromCurrency.getCode(), toCurrency.getCode(), request.effectiveDate())
                // Mesmo par e mesma data atualizam a taxa existente, evitando duplicidade na tela.
                .map(existing -> {
                    existing.updateRate(request.rate());
                    return existing;
                })
                .orElseGet(() -> new ExchangeRate(
                        fromCurrency, toCurrency, request.rate(), request.effectiveDate()));

        return ExchangeRateResponse.from(exchangeRateRepository.save(exchangeRate));
    }

    @Transactional(readOnly = true)
    public ExchangeRate findRateForDate(String fromCode, String toCode, LocalDate date) {
        // Pego a taxa mais recente até a data pedida; assim uma taxa cadastrada antes
        // continua valendo para liquidações futuras até existir uma nova taxa.
        return exchangeRateRepository
                .findFirstByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        fromCode, toCode, date)
                .orElseThrow(() -> new BusinessException(
                        String.format("Taxa de câmbio não encontrada para %s→%s em %s",
                                fromCode, toCode, date)));
    }
}
