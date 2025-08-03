package ru.kalinin.deal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.common.dto.FinishRegistrationRequestDto;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.deal.kafka.KafkaProducer;
import ru.kalinin.deal.services.DealService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/deal", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DealControllerImpl implements DealController {
    private final DealService dealService;

    private final KafkaProducer kafkaProducer;

    @Override
    @PostMapping("/statement")
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @RequestBody @Valid LoanStatementRequestDto requestDto)
    {
        log.info("POST request {} path {}", requestDto, "/deal/statement");
        return dealService.createStatement(requestDto);
    }

    @Override
    @PostMapping("/offer/select")
    public ResponseEntity<Void> selectStatement(
            @RequestBody @Valid LoanOfferDto requestDto) {
        log.info("POST request {} path {}", requestDto, "deal/offer/select");
        return dealService.selectStatement(requestDto);
    }

    @Override
    @PostMapping("/calculate/{statementId}")
    public ResponseEntity<Void> finishRegistrationAndCalculateCredit(
            @PathVariable String statementId,
            @RequestBody FinishRegistrationRequestDto requestDto) {
        log.info("POST request {} statementId {} path {}", requestDto, statementId, "deal/calculate/{statementId}");
        return dealService.finishRegistrationAndCalculateCredit(statementId, requestDto);
    }

    @Override
    @PostMapping("/document/{statementId}/send")
    public ResponseEntity<Void> sendDocOnEmail(
            @PathVariable String statementId) {
        kafkaProducer.sendMessage(statementId);
        return null;
    }

    @Override
    @PostMapping("/document/{statementId}/sign")
    public ResponseEntity<Void> signDocOnEmail(
            @PathVariable String statementId) {
        return null;
    }

    @Override
    @PostMapping("/document/{statementId}/code")
    public ResponseEntity<Void> codeDocOnEmail(
            @PathVariable String statementId) {
        return null;
    }


}
