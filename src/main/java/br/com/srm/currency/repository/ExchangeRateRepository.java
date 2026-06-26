package br.com.srm.currency.repository;

import br.com.srm.currency.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de taxas de câmbio.
 * Os métodos de nome longo são gerados automaticamente pelo Spring Data JPA
 * a partir da convenção de nomenclatura — sem necessidade de escrever SQL.
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    /** Busca taxa pelo par de moedas e data exata — usado para evitar duplicatas no upsert. */
    Optional<ExchangeRate> findByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDate(
            String fromCode,
            String toCode,
            LocalDate effectiveDate);

    /**
     * Busca a taxa mais recente disponível até a data informada.
     * O "LessThanEqual + OrderByDesc + First" garante que sempre pegamos
     * a taxa mais próxima anterior à data, sem ultrapassá-la.
     */
    Optional<ExchangeRate> findFirstByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            String fromCode,
            String toCode,
            LocalDate date);
}
