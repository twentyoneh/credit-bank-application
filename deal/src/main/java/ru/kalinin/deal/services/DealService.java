package ru.kalinin.deal.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kalinin.common.dto.FinishRegistrationRequestDto;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.beans.Statement;
import java.util.List;

public interface DealService {

    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto request);
    public ResponseEntity<Void> selectStatement(LoanOfferDto request);
    public ResponseEntity<Void> finishRegistrationAndCalculateCredit(String statementId,
                                                     FinishRegistrationRequestDto requestDto);
}
