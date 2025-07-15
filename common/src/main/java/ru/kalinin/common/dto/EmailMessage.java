package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.kalinin.common.enums.EmailTheme;

@Data
@Schema(description = "DTO для email-сообщения, содержит адрес, тему, идентификатор заявки и текст письма")
public class EmailMessage {
    @Schema(description = "Email-адрес получателя", example = "user@example.com")
    private String address;

    @Schema(description = "Тема письма", example = "APPROVED")
    private EmailTheme theme;

    @Schema(description = "ID заявки", example = "12345")
    private Long statementId;

    @Schema(description = "Текст письма", example = "Ваш кредит одобрен")
    private String text;
}