package br.com.srm.currency.dto;

import br.com.srm.currency.domain.Currency;

import java.util.UUID;

/** DTO de resposta para moedas — transfere apenas os campos necessários para o seletor de moeda no formulário. */
public record CurrencyResponse(UUID id, String code, String name) {
    public static CurrencyResponse from(Currency c) {
        return new CurrencyResponse(c.getId(), c.getCode(), c.getName());
    }
}
