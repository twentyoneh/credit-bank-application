// java
package ru.kalinin.deal.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kalinin.deal.dto.EmailMessage;
import ru.kalinin.deal.exception.VerifySesCodeException;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Credit;
import ru.kalinin.deal.models.Statement;
import ru.kalinin.deal.models.StatusHistory;
import ru.kalinin.deal.models.enums.Status;
import ru.kalinin.deal.models.enums.Theme;
import ru.kalinin.deal.repositories.StatementRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.kalinin.deal.models.enums.ChangeType.AUTOMATIC;
import static ru.kalinin.deal.models.enums.CreditStatus.ISSUED;
import static ru.kalinin.deal.models.enums.Status.DOCUMENT_SIGNED;
import static ru.kalinin.deal.models.enums.Status.PREPARE_DOCUMENTS;
import static ru.kalinin.deal.models.enums.Theme.SEND_DOCUMENTS;
import static ru.kalinin.deal.models.enums.Theme.SEND_SES;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaMessagingService kafkaMessagingService;

    @Mock
    private StatementRepository statementRepository;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private Statement newStatement(String email) {
        Statement st = new Statement();
        st.setClient(new Client());
        st.getClient().setEmail(email);
        st.setStatusHistory(new ArrayList<>());
        st.setCredit(new Credit());
        return st;
    }

    @Test
    void sendDocuments_success_updatesStatus_history_andSendsEmail() {
        String statementId = UUID.randomUUID().toString();
        Statement st = newStatement("u@example.com");
        when(statementRepository.findById(UUID.fromString(statementId))).thenReturn(Optional.of(st));

        kafkaProducerService.sendDocuments(statementId);

        assertEquals(PREPARE_DOCUMENTS, st.getStatus());
        assertEquals(1, st.getStatusHistory().size());
        StatusHistory h = st.getStatusHistory().get(0);
        assertEquals(PREPARE_DOCUMENTS, h.getStatus());
        assertEquals(AUTOMATIC, h.getChangeType());
        assertNotNull(h.getTime());

        ArgumentCaptor<EmailMessage> msg = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaMessagingService).sendMessage(eq("send-documents"), msg.capture());
        assertEquals(SEND_DOCUMENTS, msg.getValue().getTheme());
        assertEquals("u@example.com", msg.getValue().getAddress());
        assertEquals(statementId, msg.getValue().getStatementId());
    }

    @Test
    void signDocuments_setsSesCode_andSendsEmail() {
        String statementId = UUID.randomUUID().toString();
        Statement st = newStatement("u@example.com");
        when(statementRepository.findById(UUID.fromString(statementId))).thenReturn(Optional.of(st));

        kafkaProducerService.signDocuments(statementId);

        assertNotNull(st.getSesCode());

        ArgumentCaptor<EmailMessage> msg = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaMessagingService).sendMessage(eq("send-ses"), msg.capture());
        assertEquals(SEND_SES, msg.getValue().getTheme());
        assertEquals("u@example.com", msg.getValue().getAddress());
        assertEquals(statementId, msg.getValue().getStatementId());
    }

    @Test
    void verifySesCode_wrongCode_throws_andNoKafka() {
        String statementId = UUID.randomUUID().toString();
        Statement st = newStatement("u@example.com");
        st.setSesCode("111111");
        when(statementRepository.findById(UUID.fromString(statementId))).thenReturn(Optional.of(st));

        assertThrows(VerifySesCodeException.class,
                () -> kafkaProducerService.verifySesCode(statementId, "222222"));

        verifyNoInteractions(kafkaMessagingService);
        assertTrue(st.getStatusHistory().isEmpty());
    }

    @Test
    void verifySesCode_success_updatesHistory_creditIssued_andSendsEmail() {
        String statementId = UUID.randomUUID().toString();
        Statement st = newStatement("u@example.com");
        st.setSesCode("123456");
        when(statementRepository.findById(UUID.fromString(statementId))).thenReturn(Optional.of(st));

        kafkaProducerService.verifySesCode(statementId, "123456");

        // Итоговый статус и история
        assertEquals(Status.CREDIT_ISSUED, st.getStatus());
        assertEquals(2, st.getStatusHistory().size());
        assertEquals(DOCUMENT_SIGNED, st.getStatusHistory().get(0).getStatus());
        assertEquals(Status.CREDIT_ISSUED, st.getStatusHistory().get(1).getStatus());
        assertNotNull(st.getSignDate());

        // Кредит выдан
        assertEquals(ISSUED, st.getCredit().getCreditStatus());

        // Отправлено письмо
        ArgumentCaptor<EmailMessage> msg = ArgumentCaptor.forClass(EmailMessage.class);
        verify(kafkaMessagingService).sendMessage(eq("credit-issued"), msg.capture());
        assertEquals(Theme.CREDIT_ISSUED, msg.getValue().getTheme());
        assertEquals("u@example.com", msg.getValue().getAddress());
        assertEquals(statementId, msg.getValue().getStatementId());
    }

    @Test
    void sendDocuments_notFound_throwsEntityNotFound_andNoKafka() {
        String statementId = UUID.randomUUID().toString();
        when(statementRepository.findById(UUID.fromString(statementId))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> kafkaProducerService.sendDocuments(statementId));

        verifyNoInteractions(kafkaMessagingService);
    }
}