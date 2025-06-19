package ru.kalinin.deal.services;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kalinin.common.dto.FinishRegistrationRequestDto;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.beans.Statement;
import java.util.List;

public interface DealService {

    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request);
    public void selectStatement(LoanOfferDto request);
    public void finishRegistrationAndCalculateCredit(String statementId,
                                                     FinishRegistrationRequestDto requestDto);
}
