package ru.kalinin.deal.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanOffer {
    private UUID statementId;
    private BigDecimal requestedAmount;
    private BigDecimal totalAmount;
    /**
     * Срок кредита в месяцах
     */
    private Integer term;
    private BigDecimal monthlyPayment;
    /**
     * Ставка по кредиту — годовой процент за использование заёмных денег
     */
    private BigDecimal rate;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
}
