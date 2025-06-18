package ru.kalinin.deal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import ru.kalinin.deal.models.enums.CreditStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "credit_id", nullable = false)
    private UUID id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "term", nullable = false)
    private Integer term;

    @Column(name = "monthly_payment", nullable = false)
    private BigDecimal monthlyPayment;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(nullable = false)
    private BigDecimal psk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_schedule", columnDefinition = "jsonb")
    private PaymentSchedule paymentSchedule;

    @Column(name = "insurance_enabled", nullable = false)
    private Boolean insuranceEnabled;

    @Column(name = "salary_client", nullable = false)
    private Boolean salaryClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status", nullable = false)
    private CreditStatus creditStatus;
}
