package br.com.srm.currency.controller;

import br.com.srm.currency.dto.CurrencyResponse;
import br.com.srm.currency.dto.ExchangeRateRequest;
import br.com.srm.currency.dto.ExchangeRateResponse;
import br.com.srm.currency.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de câmbio: expõe endpoints para consultar moedas e gerenciar taxas de câmbio.
 *
 * O POST de exchange-rates faz upsert (cria ou atualiza) para o mesmo par de moedas
 * e data — o service garante que não haverá duplicata na tabela.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Câmbio", description = "Gerenciamento de moedas e taxas de câmbio")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/currencies")
    @Operation(summary = "Lista todas as moedas cadastradas")
    public List<CurrencyResponse> listCurrencies() {
        return currencyService.listAll();
    }

    @GetMapping("/exchange-rates")
    @Operation(summary = "Lista taxas de câmbio cadastradas")
    public List<ExchangeRateResponse> listExchangeRates() {
        return currencyService.listExchangeRates();
    }

    // 201 Created porque um novo registro pode ser gerado; se for update, ainda retorna 201 por convenção
    @PostMapping("/exchange-rates")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra ou atualiza taxa de câmbio para uma data")
    public ExchangeRateResponse createExchangeRate(@Valid @RequestBody ExchangeRateRequest request) {
        return currencyService.saveExchangeRate(request);
    }
}
