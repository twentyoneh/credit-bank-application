package ru.kalinin.statement.service;

import org.springframework.http.ResponseEntity;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.util.List;

public interface StatementService {
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto requestDto);
    public void selectOffer(LoanOfferDto offerDto);
}
