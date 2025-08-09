package ru.kalinin.deal.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kalinin.deal.dto.StatementDto;
import ru.kalinin.deal.models.Statement;
import ru.kalinin.deal.models.StatusHistory;
import ru.kalinin.deal.models.enums.ChangeType;
import ru.kalinin.deal.models.enums.StatementStatus;
import ru.kalinin.deal.models.enums.Status;
import ru.kalinin.deal.repositories.StatementRepository;
import ru.kalinin.deal.util.StatementMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {
    private final StatementRepository statementRepository;
    private final StatementMapper statementMapper;


    public void saveStatementStatus(Statement statement, Status status, ChangeType changeType) {
        log.info("Save new statement status = {}", status);

        statement.setStatus(status);
        log.info("Status saved in statement");

        var statusHistory = new StatusHistory(status, LocalDateTime.now(), changeType);
        List<StatusHistory> history = statement.getStatusHistory();
        history.add(statusHistory);
        log.info("Status saved in history: {}", history.stream()
                .map(StatusHistory::toString)
                .collect(Collectors.joining(", ")));
    }

    public void saveStatementStatus(String statementId, Status status, ChangeType changeType) {
        var statement = findStatementById(UUID.fromString(statementId));
        saveStatementStatus(statement, status, changeType);
    }

    public StatementDto findStatementById(String statementId) {
        var statement = findStatementById(UUID.fromString(statementId));
        return statementMapper.toStatementDto(statement);
    }

    private Statement findStatementById(UUID statementId) {
        var statement = statementRepository.findById(statementId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Statement with id %s wasn't found", statementId)));
        log.info("Statement found = {}", statement);
        return statement;
    }
}
