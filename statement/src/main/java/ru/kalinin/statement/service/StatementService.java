package ru.kalinin.statement.service;

import org.springframework.http.ResponseEntity;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.util.List;

public interface StatementService {
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto requestDto);
    public ResponseEntity<Void> selectOffer(LoanOfferDto offerDto);
}
