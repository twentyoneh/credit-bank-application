package ru.kalinin.deal.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import ru.kalinin.common.dto.*;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Credit;
import ru.kalinin.deal.models.Statement;
import ru.kalinin.deal.models.enums.ApplicationStatus;
import ru.kalinin.deal.models.enums.Status;
import ru.kalinin.deal.repositories.ClientRepository;
import ru.kalinin.deal.repositories.CreditRepository;
import ru.kalinin.deal.repositories.StatementRepository;
import ru.kalinin.deal.util.ClientMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class DealServiceTest {
    @Mock private ClientRepository clientRepository;
    @Mock private StatementRepository statementRepository;
    @Mock private CreditRepository creditRepository;
    @Mock private RestClient restClient;
    @Mock private KafkaMessagingService kafkaMessagingService;
    @Mock private ClientMapper clientMapper;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;
    @InjectMocks private DealServiceImpl dealService;


    @Test
    void selectStatement_SuccessfullyProcessesLoanOffer() {
        // Arrange
        UUID statementId = UUID.randomUUID();

        LoanOfferDto offerDto = new LoanOfferDto();
        offerDto.setStatementId(statementId);
        offerDto.setTotalAmount(BigDecimal.valueOf(500000));
        offerDto.setTerm(12);
        offerDto.setMonthlyPayment(BigDecimal.valueOf(43000));
        offerDto.setRate(BigDecimal.valueOf(15.5));
        offerDto.setIsInsuranceEnabled(true);
        offerDto.setIsSalaryClient(false);

        Statement statement = new Statement();
        statement.setId(statementId);
        statement.setClient(new Client());

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(creditRepository.save(any(Credit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statementRepository.save(any(Statement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dealService.selectStatement(offerDto);

        // Assert
        assertEquals(Status.APPROVED, statement.getStatus());
        assertEquals(offerDto, statement.getAppliedOffer());
        assertNotNull(statement.getCredit());
        assertEquals(offerDto.getTotalAmount(), statement.getCredit().getAmount());

        verify(creditRepository).save(any(Credit.class));
        verify(statementRepository).save(statement);
    }

}
