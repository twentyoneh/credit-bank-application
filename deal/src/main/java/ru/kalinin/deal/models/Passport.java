package ru.kalinin.deal.models;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "passport_uuid", nullable = false)
    private UUID passportId;

    @Column(name = "series", nullable = false)
    private String series;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "issue_branch", nullable = false)
    private String issueBranch;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
}
