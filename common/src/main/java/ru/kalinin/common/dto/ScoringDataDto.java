package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.kalinin.common.enums.Gender;
import ru.kalinin.common.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO для скоринговых данных, используемых при оценке заявки на кредит")
@Data
public class ScoringDataDto {
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

    @Schema(description = "Пол заёмщика", example = "MALE")
    private Gender gender;

    @Schema(description = "Дата рождения заёмщика", example = "1990-01-01")
    private LocalDate birthdate;

    @Schema(description = "Серия паспорта (4 цифры)", example = "1234")
    private String passportSeries;

    @Schema(description = "Номер паспорта (6 цифр)", example = "567890")
    private String passportNumber;

    @Schema(description = "Дата выдачи паспорта", example = "2010-05-15")
    private LocalDate passportIssueDate;

    @Schema(description = "Код подразделения, выдавшего паспорт", example = "770-001")
    private String passportIssueBranch;

    @Schema(description = "Семейное положение", example = "MARRIED")
    private MaritalStatus maritalStatus;

    @Schema(description = "Запрашиваемая сумма кредита", example = "2")
    private Integer dependentAmount;

    @Schema(description = "Информация о трудоустройстве")
    private EmploymentDto employment;

    @Schema(description = "Номер счёта для зачисления кредита", example = "40817810099910004312")
    private String accountNumber;

    @Schema(description = "Признак подключения страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Признак, что клиент получает зарплату на наш банк", example = "false")
    private Boolean isSalaryClient;
}