package ru.kalinin.deal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kalinin.deal.models.enums.EmploymentPosition;
import ru.kalinin.deal.models.enums.EmploymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "employment_uuid", nullable = false)
    private UUID employmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmploymentStatus status;

    @Column(name = "employer_inn", nullable = false)
    private String employerInn;
    @Column(name = "salary", nullable = false)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private EmploymentPosition position;

    @Column(name = "work_experience_total", nullable = false)
    private Integer workExperienceTotal;
    @Column(name = "work_experience_current", nullable = false)
    private Integer workExperienceCurrent;
}
