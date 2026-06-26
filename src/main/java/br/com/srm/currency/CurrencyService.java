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

/**
 * Serviço de câmbio: gerencia moedas e taxas de câmbio.
 *
 * A lógica de negócio aqui é simples: cadastrar taxas de câmbio (upsert por
 * par de moedas e data) e buscar a taxa vigente mais recente para uma data.
 * Essa abordagem de "taxa mais recente até a data" permite que uma taxa
 * cadastrada continue válida para liquidações futuras até que uma nova taxa
 * seja registrada.
 */
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

    /**
     * Cadastra ou atualiza uma taxa de câmbio (upsert).
     * Se já existir uma taxa para o mesmo par de moedas e a mesma data, atualiza o valor
     * em vez de criar um registro duplicado — evita inconsistência na tela de câmbio.
     */
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
                .map(existing -> {
                    existing.updateRate(request.rate());
                    return existing;
                })
                .orElseGet(() -> new ExchangeRate(
                        fromCurrency, toCurrency, request.rate(), request.effectiveDate()));

        return ExchangeRateResponse.from(exchangeRateRepository.save(exchangeRate));
    }

    /**
     * Retorna a taxa de câmbio mais recente disponível até a data informada.
     *
     * "Mais recente até a data" significa que se hoje é dia 25 e a última taxa
     * cadastrada é do dia 20, usamos a do dia 20 — a taxa continua valendo até
     * que uma nova seja cadastrada. Lança BusinessException se não houver nenhuma
     * taxa disponível para o par de moedas.
     */
    @Transactional(readOnly = true)
    public ExchangeRate findRateForDate(String fromCode, String toCode, LocalDate date) {
        return exchangeRateRepository
                .findFirstByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        fromCode, toCode, date)
                .orElseThrow(() -> new BusinessException(
                        String.format("Taxa de câmbio não encontrada para %s→%s em %s",
                                fromCode, toCode, date)));
    }
}
