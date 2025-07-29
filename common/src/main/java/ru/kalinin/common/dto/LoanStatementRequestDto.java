package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO заявки на кредит, содержит основные данные заёмщика и параметры кредита")
@Data
public class LoanStatementRequestDto {
    @Schema(description = "Запрашиваемая сумма кредита", example = "500000.00")
    private BigDecimal amount;

    @Schema(description = "Срок кредита в месяцах", example = "36")
    private Integer term;

    @Schema(description = "Имя заёмщика", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия заёмщика", example = "Иванов")
    private String lastName;

    @Schema(description = "Отчество заёмщика (может быть null)", example = "Иванович")
    private String middleName;

    @Schema(description = "Email заёмщика", example = "ivanov@example.com")
    private String email;

    @Schema(description = "Дата рождения заёмщика", example = "1990-01-01")
    private LocalDate birthdate;

    @Schema(description = "Серия паспорта (4 цифры)", example = "1234")
    private String passportSeries;

    @Schema(description = "Номер паспорта (6 цифр)", example = "567890")
    private String passportNumber;
}
