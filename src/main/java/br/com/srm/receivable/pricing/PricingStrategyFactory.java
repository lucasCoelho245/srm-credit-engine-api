package br.com.srm.receivable.pricing;

import br.com.srm.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PricingStrategyFactory {

    private final Map<String, PricingStrategy> strategies;

    public PricingStrategyFactory(List<PricingStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PricingStrategy::getName, s -> s));
    }

    public PricingStrategy resolve(String productTypeName) {
        return Optional.ofNullable(strategies.get(productTypeName))
                .orElseThrow(() -> new BusinessException(
                        "Strategy não encontrada para tipo de produto: " + productTypeName));
    }

    public Map<String, PricingStrategy> getAll() {
        return strategies;
    }
}
