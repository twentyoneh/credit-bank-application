package ru.kalinin.deal.models;

import lombok.*;
import ru.kalinin.deal.models.enums.ChangeType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StatusHistory {
    private String status;
    private LocalDateTime time;
    private ChangeType changeType;
}