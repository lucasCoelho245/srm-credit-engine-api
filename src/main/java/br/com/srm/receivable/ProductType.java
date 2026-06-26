package br.com.srm.receivable;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tipo de recebível cadastrado no banco (ex: "Duplicata Mercantil", "Cheque Pre-datado").
 *
 * Cada ProductType armazena o spreadMonthly do produto diretamente na tabela.
 * Esse valor é a fonte de verdade para o banco de dados, enquanto as classes
 * DuplicataStrategy/ChequeStrategy usam o mesmo valor para o cálculo em Java.
 * Os dois devem estar sincronizados — o seed de dados garante isso na inicialização.
 */
@Entity
@Table(name = "product_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // Spread mensal armazenado por auditabilidade — permite exibir no Swagger e no front
    @Column(name = "spread_monthly", nullable = false, precision = 5, scale = 4)
    private BigDecimal spreadMonthly;

    @Column(columnDefinition = "TEXT")
    private String description;
}
