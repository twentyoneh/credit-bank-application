package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.kalinin.common.enums.EmploymentStatus;
import ru.kalinin.common.enums.Position;

import java.math.BigDecimal;

@Schema(description = "Данные о занятости клиента")
@Data
public class EmploymentDto {
    @Schema(description = "Статус занятости", example = "EMPLOYED")
    private EmploymentStatus employmentStatus;

    @Schema(description = "ИНН работодателя", example = "7707083893")
    private String employerINN;

    @Schema(description = "Зарплата", example = "85000.00")
    private BigDecimal salary;

    @Schema(description = "Должность", example = "MIDDLE_MANAGER")
    private Position position;

    @Schema(description = "Общий трудовой стаж (в месяцах)", example = "120")
    private Integer workExperienceTotal;

    @Schema(description = "Стаж на текущем месте (в месяцах)", example = "36")
    private Integer workExperienceCurrent;
}
