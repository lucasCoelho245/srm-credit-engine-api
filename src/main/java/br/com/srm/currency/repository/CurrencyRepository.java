package br.com.srm.currency.repository;

import br.com.srm.currency.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Repositório de moedas — busca por código ISO (ex: "BRL", "USD"). */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {
    Optional<Currency> findByCode(String code);
}
