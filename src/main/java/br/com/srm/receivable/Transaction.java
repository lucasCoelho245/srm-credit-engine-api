package br.com.srm.receivable;

import br.com.srm.currency.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "cedente", "faceValue"})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String cedente;

    @Column(name = "face_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal faceValue;

    @Column(name = "present_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal presentValue;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal discount;

    @Column(name = "base_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal baseRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "title_currency_id")
    private Currency titleCurrency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_currency_id")
    private Currency paymentCurrency;

    @Column(name = "exchange_rate_used", precision = 19, scale = 6)
    private BigDecimal exchangeRateUsed;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "liquidated_at")
    private Instant liquidatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public static Transaction create(
            String cedente, BigDecimal faceValue, BigDecimal presentValue,
            BigDecimal discount, BigDecimal baseRate, Integer termMonths,
            ProductType productType, Currency titleCurrency, Currency paymentCurrency,
            BigDecimal exchangeRateUsed, LocalDate dueDate) {

        Transaction t = new Transaction();
        t.cedente = cedente;
        t.faceValue = faceValue;
        t.presentValue = presentValue;
        t.discount = discount;
        t.baseRate = baseRate;
        t.termMonths = termMonths;
        t.productType = productType;
        t.titleCurrency = titleCurrency;
        t.paymentCurrency = paymentCurrency;
        t.exchangeRateUsed = exchangeRateUsed;
        t.dueDate = dueDate;
        t.liquidatedAt = Instant.now();
        return t;
    }
}
