package ru.kalinin.deal.controller;

import org.springframework.http.ResponseEntity;
import ru.kalinin.common.dto.FinishRegistrationRequestDto;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.util.List;

public interface DealController {
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto requestDto);
    public ResponseEntity<Void> selectStatement(LoanOfferDto requestDto);
    public ResponseEntity<Void> finishRegistrationAndCalculateCredit(String statementId, FinishRegistrationRequestDto requestDto);
    public ResponseEntity<Void> sendDocOnEmail(String statementId);
    public ResponseEntity<Void> signDocOnEmail(String statementId);
    public ResponseEntity<Void> codeDocOnEmail(String statementId);
}
