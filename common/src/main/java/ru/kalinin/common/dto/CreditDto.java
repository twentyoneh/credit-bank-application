package ru.kalinin.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO с параметрами рассчитанного кредита, включая сумму, срок, ставку, ПСК, опции и график платежей")
@Builder
@Data
public class CreditDto {
    @Schema(description = "Сумма кредита", example = "500000.00")
    private BigDecimal amount;

    @Schema(description = "Срок кредита в месяцах", example = "24")
    private Integer term;

    @Schema(description = "Ежемесячный платёж", example = "23000.50")
    private BigDecimal monthlyPayment;

    @Schema(description = "Процентная ставка по кредиту", example = "12.5")
    private BigDecimal rate;

    @Schema(description = "Полная стоимость кредита (ПСК)", example = "14.2")
    private BigDecimal psk;

    @Schema(description = "Страховка включена", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Клиент является зарплатным", example = "false")
    private Boolean isSalaryClient;

    @Schema(description = "График платежей")
    private List<PaymentScheduleElementDto> paymentSchedule;
}