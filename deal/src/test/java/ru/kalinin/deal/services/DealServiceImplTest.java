// java
package ru.kalinin.deal.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import ru.kalinin.common.dto.*;
import ru.kalinin.deal.dto.EmailMessage;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Credit;
import ru.kalinin.deal.models.Statement;
import ru.kalinin.deal.models.enums.CreditStatus;
import ru.kalinin.deal.models.enums.Status;
import ru.kalinin.deal.repositories.ClientRepository;
import ru.kalinin.deal.repositories.CreditRepository;
import ru.kalinin.deal.repositories.StatementRepository;
import ru.kalinin.deal.util.ClientMapper;
import ru.kalinin.deal.util.ScoringDataMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.kalinin.deal.models.enums.Theme.CREATE_DOCUMENTS;
import static ru.kalinin.deal.models.enums.Theme.FINISH_REGISTRATION;
import static ru.kalinin.deal.models.enums.Theme.STATEMENT_DENIED;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DealServiceImplTest {

    @Mock private KafkaMessagingService kafkaMessagingService;
    @Mock private ClientRepository clientRepository;
    @Mock private StatementRepository statementRepository;
    @Mock private CreditRepository creditRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private ClientMapper clientMapper;
    @Mock private ScoringDataMapper scoringDataMapper;

    @Mock private RestClient calcClient;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    @InjectMocks private DealServiceImpl dealService;

    @BeforeEach
    void commonStubs() {
        // репозитории возвращают переданный объект
        lenient().when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(statementRepository.save(any(Statement.class))).thenAnswer(inv -> {
            Statement st = inv.getArgument(0);
            if (st.getId() == null) st.setId(UUID.randomUUID());
            return st;
        });
        lenient().when(creditRepository.save(any(Credit.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- helpers ----------

    private void mockOffersCall(List<LoanOfferDto> offers) {
        when(calcClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/calculator/offers")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanStatementRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(offers);
    }

    private void mockCalcCall(CreditDto creditDto) {
        when(calcClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/calculator/calc")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ScoringDataDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(creditDto);
    }

    // ---------- createStatement ----------

    @Test
    void createStatement_returnsOffers_andSetsStatementId() {
        LoanStatementRequestDto req = new LoanStatementRequestDto();
        Client mappedClient = new Client();
        mappedClient.setEmail("u@example.com");
        when(clientMapper.toClient(req)).thenReturn(mappedClient);

        LoanOfferDto o1 = LoanOfferDto.builder()
                .totalAmount(BigDecimal.valueOf(100))
                .build();
        LoanOfferDto o2 = LoanOfferDto.builder()
                .totalAmount(BigDecimal.valueOf(200))
                .build();
        mockOffersCall(List.of(o1, o2));

        ResponseEntity<List<LoanOfferDto>> resp = dealService.createStatement(req);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());

        ArgumentCaptor<Statement> stCaptor = ArgumentCaptor.forClass(Statement.class);
        verify(statementRepository).save(stCaptor.capture());
        UUID stId = stCaptor.getValue().getId();
        assertNotNull(stId);

        for (LoanOfferDto o : resp.getBody()) {
            assertEquals(stId, o.getStatementId());
        }
        verify(kafkaMessagingService, never()).sendMessage(eq("statement-denied"), any());
    }

    @Test
    void createStatement_noOffers_sendsDeniedEmail_andReturns422() {
        LoanStatementRequestDto req = new LoanStatementRequestDto();
        Client mappedClient = new Client();
        mappedClient.setEmail("u@example.com");
        when(clientMapper.toClient(req)).thenReturn(mappedClient);

        mockOffersCall(Collections.emptyList());

        ResponseEntity<List<LoanOfferDto>> resp = dealService.createStatement(req);
        assertEquals(422, resp.getStatusCodeValue());

        ArgumentCaptor<EmailMessage> msg = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaMessagingService).sendMessage(eq("statement-denied"), msg.capture());
        assertEquals(STATEMENT_DENIED, msg.getValue().getTheme());
    }

    // ---------- selectStatement ----------

    @Test
    void selectStatement_success_updatesStatement_savesCredit_andSendsFinishRegistration() {
        UUID stId = UUID.randomUUID();
        LoanOfferDto offer = LoanOfferDto.builder()
                .statementId(stId)
                .totalAmount(BigDecimal.valueOf(500_000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(43_000))
                .rate(BigDecimal.valueOf(15.5))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        Statement st = new Statement();
        st.setId(stId);
        st.setClient(new Client());
        st.setStatusHistory(new ArrayList<>());

        when(statementRepository.findById(stId)).thenReturn(Optional.of(st));

        dealService.selectStatement(offer);

        assertEquals(Status.APPROVED, st.getStatus());
        assertEquals(offer, st.getAppliedOffer());
        assertNotNull(st.getCredit());
        assertEquals(offer.getTotalAmount(), st.getCredit().getAmount());

        verify(creditRepository).save(any(Credit.class));
        verify(statementRepository).save(same(st));

        ArgumentCaptor<EmailMessage> msg = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaMessagingService).sendMessage(eq("finish-registration"), msg.capture());
        assertEquals(FINISH_REGISTRATION, msg.getValue().getTheme());
    }

    @Test
    void selectStatement_statementNotFound_throws_andDoesNotSaveOrSend() {
        UUID stId = UUID.randomUUID();
        LoanOfferDto offer = LoanOfferDto.builder().
                statementId(stId).
                build();
        when(statementRepository.findById(stId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> dealService.selectStatement(offer));
        verifyNoInteractions(creditRepository);
        verify(statementRepository, never()).save(any());
        verifyNoInteractions(kafkaMessagingService);
    }

    // ---------- finishRegistrationAndCalculateCredit ----------

    @Test
    void finishRegistration_success_savesFinalCredit_updatesStatusHistory_andSendsCreateDocuments() throws Exception {
        UUID stId = UUID.randomUUID();
        String stIdStr = stId.toString();

        Client client = new Client();
        client.setEmail("u@example.com");

        Credit initialCredit = new Credit();
        initialCredit.setId(UUID.randomUUID());

        Statement st = new Statement();
        st.setId(stId);
        st.setClient(client);
        st.setCredit(initialCredit);
        st.setStatusHistory(new ArrayList<>());

        when(statementRepository.findById(stId)).thenReturn(Optional.of(st));

        FinishRegistrationRequestDto finReq = new FinishRegistrationRequestDto();
        ScoringDataDto scoring = new ScoringDataDto();
        when(scoringDataMapper.toScoringDataDto(client, st, finReq)).thenReturn(scoring);

        CreditDto creditDto = CreditDto.builder()
            .amount(BigDecimal.valueOf(500_000))
            .term(24)
            .monthlyPayment(BigDecimal.valueOf(25_000))
            .rate(BigDecimal.valueOf(10.5))
            .psk(BigDecimal.valueOf(12.3))
            .isInsuranceEnabled(true)
            .isSalaryClient(true)
            .paymentSchedule(List.of())
            .build();
        mockCalcCall(creditDto);

        ResponseEntity<Void> resp = dealService.finishRegistrationAndCalculateCredit(stIdStr, finReq);
        assertEquals(200, resp.getStatusCodeValue());

        ArgumentCaptor<Credit> creditCaptor = ArgumentCaptor.forClass(Credit.class);
        verify(creditRepository).save(creditCaptor.capture());
        Credit saved = creditCaptor.getValue();
        assertEquals(creditDto.getAmount(), saved.getAmount());
        assertEquals(creditDto.getTerm(), saved.getTerm());
        assertEquals(CreditStatus.CALCULATED, saved.getCreditStatus());

        verify(statementRepository).save(same(st));
        assertEquals(1, st.getStatusHistory().size());
        assertEquals(Status.CC_APPROVED, st.getStatusHistory().get(0).getStatus());

        ArgumentCaptor<EmailMessage> msg = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaMessagingService).sendMessage(eq("create-documents"), msg.capture());
        assertEquals(CREATE_DOCUMENTS, msg.getValue().getTheme());
        assertEquals(stIdStr, msg.getValue().getStatementId());
    }

    @Test
    void finishRegistration_calcError_throws_andDoesNotSendEmail() {
        UUID stId = UUID.randomUUID();
        Statement st = new Statement();
        st.setId(stId);
        st.setClient(new Client());
        st.setCredit(new Credit());

        when(statementRepository.findById(stId)).thenReturn(Optional.of(st));
        when(scoringDataMapper.toScoringDataDto(any(), any(), any())).thenReturn(new ScoringDataDto());

        // эмулируем падение при вызове калькулятора
        when(calcClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/calculator/calc")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ScoringDataDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("down"));

        assertThrows(RuntimeException.class,
                () -> dealService.finishRegistrationAndCalculateCredit(stId.toString(), new FinishRegistrationRequestDto()));

        verifyNoInteractions(creditRepository);
        verify(statementRepository, never()).save(any());
        verifyNoInteractions(kafkaMessagingService);
    }

    @Test
    void finishRegistration_statementSaveError_sendsDeniedEmail_andRethrows() {
        UUID stId = UUID.randomUUID();
        Client client = new Client();
        client.setEmail("u@example.com");
        Statement st = new Statement();
        st.setId(stId);
        st.setClient(client);
        Credit cr = new Credit();
        cr.setId(UUID.randomUUID());
        st.setCredit(cr);

        when(statementRepository.findById(stId)).thenReturn(Optional.of(st));
        when(scoringDataMapper.toScoringDataDto(any(), any(), any())).thenReturn(new ScoringDataDto());
        mockCalcCall(CreditDto.builder().build()); // валидный ответ
        when(statementRepository.save(any(Statement.class))).thenThrow(new RuntimeException("db error"));

        assertThrows(RuntimeException.class,
                () -> dealService.finishRegistrationAndCalculateCredit(stId.toString(), new FinishRegistrationRequestDto()));

        ArgumentCaptor<EmailMessage> msg = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaMessagingService).sendMessage(eq("statement-denied"), msg.capture());
        assertEquals(STATEMENT_DENIED, msg.getValue().getTheme());
    }
}