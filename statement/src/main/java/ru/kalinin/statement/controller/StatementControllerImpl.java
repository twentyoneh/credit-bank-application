package ru.kalinin.statement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.statement.service.StatementService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/statement", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class StatementControllerImpl implements StatementController {
    private final StatementService statementService;

    @Override
    @PostMapping
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @RequestBody @Valid LoanStatementRequestDto requestDto) {
        log.info("POST request {} path {}", requestDto, "/statement");
        return ResponseEntity.ok(statementService.createStatement(requestDto));
    }

    @Override
    @PostMapping("/offer")
    public ResponseEntity<Void> selectOffer(
            @RequestBody @Valid LoanOfferDto offerDto) {
        log.info("POST request {} path {}", offerDto, "/statement/offer");
        statementService.selectOffer(offerDto);
        return null;
    }


}
