package ru.kalinin.deal.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kalinin.deal.exception.VerifySesCodeException;
import ru.kalinin.deal.models.enums.Status;
import ru.kalinin.deal.models.enums.Theme;
import ru.kalinin.deal.repositories.StatementRepository;
import ru.kalinin.deal.util.SesCodeGenerator;
import ru.kalinin.deal.dto.EmailMessage;
import ru.kalinin.deal.models.Statement;

import java.time.LocalDateTime;
import java.util.UUID;

import static ru.kalinin.deal.models.enums.ChangeType.AUTOMATIC;
import static ru.kalinin.deal.models.enums.CreditStatus.ISSUED;
import static ru.kalinin.deal.models.enums.Status.DOCUMENT_SIGNED;
import static ru.kalinin.deal.models.enums.Status.PREPARE_DOCUMENTS;
import static ru.kalinin.deal.models.enums.Theme.SEND_DOCUMENTS;
import static ru.kalinin.deal.models.enums.Theme.SEND_SES;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducerService {
    private final KafkaMessagingService kafkaMessagingService;
    private final StatementRepository statementRepository;
    private final AdminService adminService;

    public void sendDocuments(String statementId) {
        log.info("Create kafka message to send documents for statementId = {}", statementId);

        var statement = findStatementById(UUID.fromString(statementId));

        adminService.saveStatementStatus(statement, PREPARE_DOCUMENTS, AUTOMATIC);

        var emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(SEND_DOCUMENTS)
                .statementId(statementId)
                .build();
        kafkaMessagingService.sendMessage("send-documents", emailMessage);
    }

    public void signDocuments(String statementId) {
        log.info("Create kafka message to send sesCode for statementId = {}", statementId);

        var statement = findStatementById(UUID.fromString(statementId));

        int sesCode = SesCodeGenerator.generateSesCode();
        statement.setSesCode(String.valueOf(sesCode));
        log.info("SesCode generated = {} and saved to statement", sesCode);

        var emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(SEND_SES)
                .statementId(statementId)
                .build();
        kafkaMessagingService.sendMessage("send-ses", emailMessage);
    }

    public void verifySesCode(String statementId, String sesCode) {
        log.info("Create kafka message to verify sesCode = {} and issue credit for statementId = {}", sesCode, statementId);

        var statement = findStatementById(UUID.fromString(statementId));

        if (!statement.getSesCode().equals(sesCode)) {
            log.info("Ses code is invalid.");
            throw new VerifySesCodeException("Ses code is invalid.");
        }

        adminService.saveStatementStatus(statement, DOCUMENT_SIGNED, AUTOMATIC);
        log.info("Credit documents signed.");
        statement.setSignDate(LocalDateTime.now());
        statement.getCredit().setCreditStatus(ISSUED);
        adminService.saveStatementStatus(statement, Status.CREDIT_ISSUED, AUTOMATIC);
        log.info("Credit issued.");

        var emailMessage = EmailMessage.builder()
                .address(statement.getClient().getEmail())
                .theme(Theme.CREDIT_ISSUED)
                .statementId(statementId)
                .build();
        kafkaMessagingService.sendMessage("credit-issued", emailMessage);
    }

    private Statement findStatementById(UUID statementId) {
        var statement = statementRepository.findById(statementId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Statement with id %s wasn't found", statementId)));
        log.info("Statement found = {}", statement);
        return statement;
    }


}
