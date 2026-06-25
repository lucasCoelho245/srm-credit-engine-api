package br.com.srm.currency;

import br.com.srm.currency.dto.CurrencyResponse;
import br.com.srm.currency.dto.ExchangeRateRequest;
import br.com.srm.currency.dto.ExchangeRateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/exchange-rates")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra ou atualiza taxa de câmbio para uma data")
    public ExchangeRateResponse createExchangeRate(@Valid @RequestBody ExchangeRateRequest request) {
        return currencyService.saveExchangeRate(request);
    }
}
