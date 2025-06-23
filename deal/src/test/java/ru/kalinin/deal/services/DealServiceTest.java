package ru.kalinin.deal.services;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @InjectMocks private DealServiceImpl dealService;


    @Test
    void testCreateStatement_ReturnsLoanOffers() {
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

        List<LoanOfferDto> expectedOffers = List.of(new LoanOfferDto(), new LoanOfferDto(), new LoanOfferDto(),new LoanOfferDto());

        when(clientRepository.save(any())).thenReturn(savedClient);
        when(statementRepository.save(any())).thenReturn(savedStatement);

        // Мокаем только RequestBodyUriSpec и строим цепочку вызовов на нем
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/calculator/offers")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedOffers);

        // Вызов метода
        List<LoanOfferDto> actualOffers = dealService.createStatement(request);

        // Проверки
        assertNotNull(actualOffers);
        assertEquals(4, actualOffers.size());
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

    @Test
    void finishRegistrationAndCalculateCredit_Success() throws Exception {
        // Arrange
        String statementIdStr = UUID.randomUUID().toString();
        UUID statementId = UUID.fromString(statementIdStr);

        Client client = Client.builder()
                .firstName("John")
                .lastName("Doe")
                .middleName("M")
                .birthDate(LocalDate.of(1990, 1, 1))
                .passport(Passport.builder().series("1234").number("567890").build())
                .accountNumber("acc123")
                .build();

        Credit credit = Credit.builder()
                .id(UUID.randomUUID())
                .term(12)
                .insuranceEnabled(true)
                .salaryClient(false)
                .build();

        Statement statement = new Statement();
        statement.setId(statementId);
        statement.setClient(client);
        statement.setCredit(credit);

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        requestDto.setGender(Gender.MALE);
        requestDto.setMaritalStatus(MaritalStatus.MARRIED);
        requestDto.setDependentAmount(500000);
        requestDto.setPassportIssueDate(LocalDate.of(2015, 6, 15));
        requestDto.setPassportIssueBrach("770-001");
        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setEmployerINN("7707083893");
        employment.setSalary(new BigDecimal("120000.00"));
        employment.setPosition(Position.MID_MANAGER);
        employment.setWorkExperienceTotal(84);
        employment.setWorkExperienceCurrent(24);
        requestDto.setEmployment(employment);
        requestDto.setAccountNumber("40817810099910004312");


        ScoringDataDto scoringData = new ScoringDataDto();
        CreditDto creditDto = new CreditDto();
        creditDto.setAmount(BigDecimal.valueOf(300000));
        creditDto.setTerm(12);
        creditDto.setMonthlyPayment(BigDecimal.valueOf(27000));
        creditDto.setRate(BigDecimal.valueOf(13.5));
        creditDto.setPsk(BigDecimal.valueOf(14.2));
        creditDto.setIsInsuranceEnabled(true);
        creditDto.setIsSalaryClient(false);
        creditDto.setPaymentSchedule(List.of());

        String jsonBody = "{}";

        RestClient.RequestBodyUriSpec requestBodySpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(objectMapper.writeValueAsString(any(ScoringDataDto.class))).thenReturn(jsonBody);

        when(restClient.post()).thenReturn(requestBodySpec);
        when(requestBodySpec.uri("/calculator/calc")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(jsonBody)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(creditDto);

        when(creditRepository.save(any(Credit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statementRepository.save(any(Statement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dealService.finishRegistrationAndCalculateCredit(statementIdStr, requestDto);

        verify(statementRepository).findById(statementId);
        when(objectMapper.writeValueAsString(any())).thenReturn(jsonBody);
        verify(objectMapper).writeValueAsString(any());
        verify(restClient).post();
        verify(creditRepository).save(any(Credit.class));
        verify(statementRepository).save(statement);

        assertEquals(CreditStatus.CALCULATED, statement.getCredit().getCreditStatus());
    }
}
