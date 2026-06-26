package br.com.srm.receivable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório de transações: operações de escrita (save) e leitura simples.
 *
 * Herda de JpaRepository, que já fornece save, findById, findAll e delete.
 * Não declaramos queries customizadas aqui — as consultas analíticas com filtros
 * ficam no ReportRepository para separar responsabilidades.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findAll(Pageable pageable);
}
