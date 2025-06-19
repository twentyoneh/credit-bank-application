package ru.kalinin.deal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.kalinin.deal.models.enums.Gender;
import ru.kalinin.deal.models.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "client_id", nullable = false)
    private UUID id;  // PK

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name", nullable = false)
    private String middleName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status",nullable = true)
    private MaritalStatus maritalStatus;

    @Column(name = "dependent_amount", nullable = false)
    private BigDecimal dependentAmount;

    @OneToOne
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "passport_id", columnDefinition = "jsonb")
    private Passport passport; //FK

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "employment_id", columnDefinition = "jsonb")
    private UUID employmentId;    //FK

    @Column(name = "account_number", nullable = true)
    private String accountNumber;

}
