package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.kalinin.common.enums.ChangeType;
import ru.kalinin.common.enums.StatementStatus;

import java.time.LocalDateTime;

@Schema(description = "DTO истории изменения статуса заявки на кредит")
@Data
public class StatementStatusHistoryDto {
    @Schema(description = "Статус заявки", example = "APPROVED")
    private StatementStatus status;

    @Schema(description = "Время изменения статуса", example = "2024-06-01T12:34:56")
    private LocalDateTime time;

    @Schema(description = "Тип изменения статуса", example = "MANUAL")
    private ChangeType changeType;
}
