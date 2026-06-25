package br.com.srm.reports;

import br.com.srm.receivable.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Transaction, UUID> {

    @Query(value = """
            SELECT t.id, t.cedente, t.face_value, t.present_value, t.discount,
                   c_pay.code AS payment_currency_code, c_title.code AS title_currency_code,
                   pt.name AS product_type_name, t.base_rate, t.term_months,
                   t.exchange_rate_used, t.due_date, t.liquidated_at, t.created_at
            FROM transactions t
            JOIN currencies c_pay ON c_pay.id = t.payment_currency_id
            JOIN currencies c_title ON c_title.id = t.title_currency_id
            JOIN product_types pt ON pt.id = t.product_type_id
            WHERE (:startDate IS NULL OR t.created_at >= :startDate)
              AND (:endDate IS NULL OR t.created_at <= :endDate)
              AND (:cedente IS NULL OR LOWER(t.cedente) LIKE LOWER(CONCAT('%', :cedente, '%')))
              AND (:paymentCurrencyCode IS NULL OR c_pay.code = :paymentCurrencyCode)
            ORDER BY t.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM transactions t
            JOIN currencies c_pay ON c_pay.id = t.payment_currency_id
            WHERE (:startDate IS NULL OR t.created_at >= :startDate)
              AND (:endDate IS NULL OR t.created_at <= :endDate)
              AND (:cedente IS NULL OR LOWER(t.cedente) LIKE LOWER(CONCAT('%', :cedente, '%')))
              AND (:paymentCurrencyCode IS NULL OR c_pay.code = :paymentCurrencyCode)
            """,
            nativeQuery = true)
    Page<Object[]> findExtract(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("cedente") String cedente,
            @Param("paymentCurrencyCode") String paymentCurrencyCode,
            Pageable pageable);
}
