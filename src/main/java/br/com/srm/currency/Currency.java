package br.com.srm.currency;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "currencies")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "code"})
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
