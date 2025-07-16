package ru.kalinin.calculator.service;

import org.springframework.http.ResponseEntity;
import ru.kalinin.common.dto.CreditDto;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.common.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    ResponseEntity<List<LoanOfferDto>> calculateOffers(LoanStatementRequestDto request);
    ResponseEntity<CreditDto> calculateCredit(ScoringDataDto data);
}
