package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.kalinin.common.enums.Gender;
import ru.kalinin.common.enums.MaritalStatus;

import java.time.LocalDate;

@Schema(description = "DTO для завершения регистрации клиента")
@Data
public class FinishRegistrationRequestDto {
    @Schema(description = "Пол клиента", example = "MALE")
    private Gender gender;

    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @Schema(description = "Количество иждивенцев", example = "2")
    private Integer dependentAmount;

    @Schema(description = "Дата выдачи паспорта", example = "2015-06-15")
    private LocalDate passportIssueDate;

    @Schema(description = "Код подразделения, выдавшего паспорт", example = "770-001")
    private String passportIssueBrach;

    @Schema(description = "Данные о занятости")
    private EmploymentDto employment;

    @Schema(description = "Номер счёта клиента", example = "40817810099910004312")
    private String accountNumber;
}