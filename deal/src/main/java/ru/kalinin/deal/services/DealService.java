package ru.kalinin.deal.services;

import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.beans.Statement;

public interface DealService {
    Statement saveStatement(LoanStatementRequestDto requestDto);
}
