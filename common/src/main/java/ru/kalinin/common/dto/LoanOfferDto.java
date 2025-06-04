package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO предложения по кредиту, содержит параметры одобренного кредита")
@Data
public class LoanOfferDto {
    @Schema(description = "Идентификатор заявки", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID statementId;

    @Schema(description = "Запрошенная сумма кредита", example = "500000.00")
    private BigDecimal requestedAmount;

    @Schema(description = "Итоговая сумма кредита (с учётом страховки, если выбрана)", example = "520000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Срок кредита в месяцах", example = "36")
    private Integer term;

    @Schema(description = "Ежемесячный платёж по кредиту", example = "16000.00")
    private BigDecimal monthlyPayment;

    @Schema(description = "Итоговая процентная ставка по кредиту", example = "12.5")
    private BigDecimal rate;

    @Schema(description = "Признак подключения страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Признак, что клиент получает зарплату на наш банк", example = "false")
    private Boolean isSalaryClient;
}
