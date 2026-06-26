package br.com.srm.receivable;

import br.com.srm.currency.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA que representa uma liquidação de recebível gravada no banco.
 *
 * Cada registro aqui é imutável por design: uma liquidação nunca é editada,
 * apenas criada. Por isso todos os campos são privados sem setter, e o único
 * jeito de criar uma Transaction é pelo método estático create().
 *
 * Lombok elimina o boilerplate:
 *   - @Getter gera os getters de todos os campos
 *   - @NoArgsConstructor(PROTECTED) cria construtor sem args só para o JPA usar internamente
 *   - @EqualsAndHashCode(of = "id") compara entidades pelo ID, não por valor de campo
 *   - @ToString exibe apenas campos seguros nos logs, sem carregar os joins lazy
 */
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

    // FetchType.LAZY: o JPA só vai ao banco buscar o ProductType quando o campo for acessado,
    // não automaticamente ao carregar a Transaction. Melhora performance em listagens.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "title_currency_id")
    private Currency titleCurrency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_currency_id")
    private Currency paymentCurrency;

    // Nulo quando a operação é em moeda única (BRL→BRL). Preenchido só em cross-currency.
    @Column(name = "exchange_rate_used", precision = 19, scale = 6)
    private BigDecimal exchangeRateUsed;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "liquidated_at")
    private Instant liquidatedAt;

    // updatable = false: uma vez gravado, o createdAt nunca é alterado pelo JPA
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /**
     * Único ponto de criação de Transaction. Static factory em vez de construtor
     * público evita que alguém crie uma Transaction sem preencher os campos obrigatórios.
     */
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
