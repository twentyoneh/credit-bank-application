package ru.kalinin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Элемент графика платежей по кредиту")
@NoArgsConstructor
@Data
public class PaymentScheduleElementDto {
    @Schema(description = "Номер платежа в графике", example = "1")
    private Integer number;

    @Schema(description = "Дата платежа", example = "2024-07-01")
    private LocalDate date;

    @Schema(description = "Общий платёж по кредиту за период", example = "16000.00")
    private BigDecimal totalPayment;

    @Schema(description = "Платёж по процентам за период", example = "2000.00")
    private BigDecimal interestPayment;

    @Schema(description = "Платёж по основному долгу за период", example = "14000.00")
    private BigDecimal debtPayment;

    @Schema(description = "Оставшаяся сумма основного долга после платежа", example = "486000.00")
    private BigDecimal remainingDebt;

    @Builder
    public PaymentScheduleElementDto(Integer number,
                                     LocalDate date,
                                     BigDecimal totalPayment,
                                     BigDecimal interestPayment,
                                     BigDecimal debtPayment,
                                     BigDecimal remainingDebt) {
        this.number = number;
        this.date = date;
        this.totalPayment = totalPayment;
        this.interestPayment = interestPayment;
        this.debtPayment = debtPayment;
        this.remainingDebt = remainingDebt;
    }
}