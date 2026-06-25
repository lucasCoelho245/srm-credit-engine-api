package br.com.srm.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    Optional<ExchangeRate> findByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDate(
            String fromCode,
            String toCode,
            LocalDate effectiveDate);

    Optional<ExchangeRate> findFirstByFromCurrency_CodeAndToCurrency_CodeAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            String fromCode,
            String toCode,
            LocalDate date);
}
