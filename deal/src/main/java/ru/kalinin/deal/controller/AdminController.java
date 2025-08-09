package ru.kalinin.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.deal.dto.StatementDto;
import ru.kalinin.deal.services.AdminService;
import ru.kalinin.deal.models.enums.Status;

import java.util.List;

import static ru.kalinin.deal.models.enums.ChangeType.MANUAL;

@Tag(name = "Сделка: админ", description = "API по сохранению нового статуса заявки и получению заявки по идентификтору.")
@RequiredArgsConstructor
@RestController
@RequestMapping("/deal/admin/statement")
public class AdminController {

    private final AdminService adminService;

    /**
     * Change statement status.
     */
    @Operation(summary = "Сохранение нового статуса заявки.")
    @GetMapping
    public ResponseEntity<List<StatementDto>> getAllStatement() {
        return  adminService.findAllStatements();
    }

    /**
     * Get statement by id.
     */
    @Operation(summary = "Получение заявки по идентификатору.")
    @GetMapping("/{statementId}")
    public ResponseEntity<StatementDto> findStatementById(@PathVariable @Parameter(required = true) String statementId) {
        return adminService.findStatementById(statementId);
    }
}