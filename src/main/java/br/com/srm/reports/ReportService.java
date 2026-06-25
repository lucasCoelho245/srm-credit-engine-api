package br.com.srm.reports;

import br.com.srm.receivable.Transaction;
import br.com.srm.receivable.TransactionRepository;
import br.com.srm.reports.dto.TransactionPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Page<TransactionPageResponse> listTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::mapTransaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionPageResponse> extract(
            Instant startDate, Instant endDate,
            String cedente, String paymentCurrencyCode, Pageable pageable) {

        // O extrato usa uma consulta própria para conseguir filtrar por período,
        // cedente e moeda sem carregar mais dados do que a tela precisa.
        return reportRepository.findExtract(startDate, endDate, cedente, paymentCurrencyCode, pageable)
                .map(this::mapRow);
    }

    private TransactionPageResponse mapTransaction(Transaction t) {
        return new TransactionPageResponse(
                t.getId(), t.getCedente(), t.getFaceValue(), t.getPresentValue(),
                t.getDiscount(), t.getPaymentCurrency().getCode(), t.getTitleCurrency().getCode(),
                t.getProductType().getName(), t.getBaseRate(), t.getTermMonths(),
                t.getExchangeRateUsed(), t.getDueDate(), t.getLiquidatedAt(), t.getCreatedAt()
        );
    }

    private TransactionPageResponse mapRow(Object[] row) {
        // A consulta nativa volta como Object[]; esses conversores deixam o mapeamento
        // compatível tanto com H2 nos testes quanto com PostgreSQL no Docker.
        return new TransactionPageResponse(
                toUuid(row[0]),
                (String) row[1],
                toBD(row[2]), toBD(row[3]), toBD(row[4]),
                (String) row[5], (String) row[6], (String) row[7],
                toBD(row[8]),
                row[9] != null ? ((Number) row[9]).intValue() : null,
                toBD(row[10]),
                toLocalDate(row[11]),
                toInstant(row[12]),
                toInstant(row[13])
        );
    }

    private java.util.UUID toUuid(Object val) {
        if (val == null) return null;
        if (val instanceof java.util.UUID uuid) return uuid;
        if (val instanceof byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return new java.util.UUID(buffer.getLong(), buffer.getLong());
        }
        return java.util.UUID.fromString(val.toString());
    }

    private BigDecimal toBD(Object val) {
        if (val == null) return null;
        return new BigDecimal(val.toString());
    }

    private LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDate localDate) return localDate;
        if (val instanceof java.sql.Date date) return date.toLocalDate();
        if (val instanceof Timestamp timestamp) return timestamp.toLocalDateTime().toLocalDate();
        return LocalDate.parse(val.toString().substring(0, 10));
    }

    private Instant toInstant(Object val) {
        if (val == null) return null;
        if (val instanceof Instant instant) return instant;
        if (val instanceof Timestamp timestamp) return timestamp.toInstant();
        if (val instanceof OffsetDateTime offsetDateTime) return offsetDateTime.toInstant();
        if (val instanceof LocalDateTime localDateTime) return localDateTime.toInstant(ZoneOffset.UTC);

        String text = val.toString();
        try {
            return Instant.parse(text);
        } catch (RuntimeException ignored) {
            return LocalDateTime.parse(text.replace(' ', 'T')).toInstant(ZoneOffset.UTC);
        }
    }
}
