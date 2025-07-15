package ru.kalinin.calculator.service;

import ru.kalinin.common.dto.CreditDto;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.common.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request);
    CreditDto calculateCredit(ScoringDataDto data);
}
