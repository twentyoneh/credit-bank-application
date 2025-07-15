package ru.kalinin.deal.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import ru.kalinin.common.dto.*;
import ru.kalinin.common.enums.EmploymentStatus;
import ru.kalinin.common.enums.MaritalStatus;
import ru.kalinin.common.enums.Position;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Credit;
import ru.kalinin.deal.models.Passport;
import ru.kalinin.deal.models.Statement;
import ru.kalinin.deal.models.enums.ApplicationStatus;
import ru.kalinin.common.enums.Gender;
import ru.kalinin.deal.models.enums.CreditStatus;
import ru.kalinin.deal.repositories.ClientRepository;
import ru.kalinin.deal.repositories.CreditRepository;
import ru.kalinin.deal.repositories.StatementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class DealServiceTest {
    @Mock private ClientRepository clientRepository;
    @Mock private StatementRepository statementRepository;
    @Mock private CreditRepository creditRepository;
    @Mock private RestClient restClient;
    @Mock private ObjectMapper objectMapper;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;
    @InjectMocks private DealServiceImpl dealService;


    @Test
    void testCreateStatement_ReturnsLoanOffers() throws JsonProcessingException {

        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setAmount(BigDecimal.valueOf(500000.00));
        request.setTerm(36);
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setMiddleName("Jones");
        request.setEmail("john@mail.ru");
        request.setBirthdate(LocalDate.of(1998, 5, 24));
        request.setPassportNumber("213513");
        request.setPassportSeries("8789");

        Client savedClient = new Client();
        savedClient.setId(UUID.randomUUID());

        Statement savedStatement = new Statement();
        savedStatement.setId(UUID.randomUUID());
        savedStatement.setClient(savedClient);
        var statementId = savedStatement.getId();

        List<LoanOfferDto> expectedOffers = List.of(new LoanOfferDto(), new LoanOfferDto(), new LoanOfferDto(),new LoanOfferDto());

        when(clientRepository.save(any())).thenReturn(savedClient);
        when(statementRepository.save(any())).thenReturn(savedStatement);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/calculator/offers")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedOffers);

        List<LoanOfferDto> result = dealService.createStatement(request).getBody();

        assertNotNull(result);
        assertEquals(4, result.size());
    }

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
        assertEquals(ApplicationStatus.APPROVED, statement.getStatus());
        assertEquals(offerDto, statement.getAppliedOffer());
        assertNotNull(statement.getCredit());
        assertEquals(offerDto.getTotalAmount(), statement.getCredit().getAmount());

        verify(creditRepository).save(any(Credit.class));
        verify(statementRepository).save(statement);
    }

}
