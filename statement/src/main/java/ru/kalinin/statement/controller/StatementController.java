package ru.kalinin.statement.controller;

import org.springframework.http.ResponseEntity;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.util.List;

public interface StatementController {
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto requestDto);
    public ResponseEntity<Void> selectOffer(LoanOfferDto offerDto);
}
