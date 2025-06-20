package ru.kalinin.deal.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "passport_uuid", nullable = false)
    private UUID id;

    @Column(name = "series", nullable = false)
    private String series;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "issue_branch", nullable = true)
    private String issueBranch;

    @Column(name = "issue_date", nullable = true)
    private LocalDate issueDate;
}
