package ru.kalinin.deal.services;

import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.deal.repositories.ClientRepository;
import ru.kalinin.deal.repositories.CreditRepository;
import ru.kalinin.deal.repositories.StatementRepository;

import java.beans.Statement;

public class DealServiceImpl implements DealService {
    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
//    private final ModelMapper mapper;
//    private final MessagingService messagingService;

    Statement saveStatement(LoanStatementRequestDto requestDto){
        Statement statement = statementRepository.save(new Statement().builder().);
    }
}
