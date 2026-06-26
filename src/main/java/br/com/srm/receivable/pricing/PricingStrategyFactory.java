package br.com.srm.receivable.pricing;

import br.com.srm.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Fábrica que seleciona a estratégia de precificação correta para cada tipo de produto.
 *
 * O Spring injeta automaticamente todas as classes que implementam PricingStrategy
 * (DuplicataStrategy, ChequeStrategy, etc.) como uma lista no construtor. A factory
 * transforma essa lista em um mapa chaveado pelo nome do produto, para busca em O(1).
 *
 * Esse design permite adicionar um novo tipo de recebível criando apenas uma nova
 * classe que implemente PricingStrategy — sem alterar a factory nem o motor de cálculo.
 */
@Component
public class PricingStrategyFactory {

    private final Map<String, PricingStrategy> strategies;

    public PricingStrategyFactory(List<PricingStrategy> strategyList) {
        // Constrói o mapa na inicialização: nome do produto -> estratégia correspondente
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PricingStrategy::getName, s -> s));
    }

    /**
     * Retorna a estratégia para o nome do produto informado.
     * Lança BusinessException se o nome não tiver estratégia cadastrada.
     */
    public PricingStrategy resolve(String productTypeName) {
        return Optional.ofNullable(strategies.get(productTypeName))
                .orElseThrow(() -> new BusinessException(
                        "Strategy não encontrada para tipo de produto: " + productTypeName));
    }

    /** Expõe todas as estratégias registradas — útil para Swagger e testes. */
    public Map<String, PricingStrategy> getAll() {
        return strategies;
    }
}
