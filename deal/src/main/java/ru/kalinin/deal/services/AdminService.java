package ru.kalinin.deal.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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


    public ResponseEntity<List<StatementDto>> findAllStatements() {
        return ResponseEntity.ok(statementRepository.findAll().stream()
                .map(statementMapper::toStatementDto)
                .collect(Collectors.toList()));
    }

    public ResponseEntity<StatementDto> findStatementById(String statementId) {
        try {
            var statement = findStatementById(UUID.fromString(statementId));
            return ResponseEntity.ok(statementMapper.toStatementDto(statement));
        } catch (EntityNotFoundException ex) {
            log.error("Ошибка поиска заявления: {}", ex.getMessage());
            return ResponseEntity.noContent().build();
        }
    }

    private Statement findStatementById(UUID statementId) {
        var statement = statementRepository.findById(statementId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Statement with id %s wasn't found", statementId)));
        log.info("Statement found = {}", statement);
        return statement;
    }
}
