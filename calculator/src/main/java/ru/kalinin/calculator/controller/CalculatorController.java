package ru.kalinin.calculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kalinin.common.dto.CreditDto;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.common.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorController {
    ResponseEntity<List<LoanOfferDto>> getLoanOffers(@RequestBody LoanStatementRequestDto request);
    ResponseEntity<CreditDto> calculateCredit(@RequestBody ScoringDataDto data);
}
