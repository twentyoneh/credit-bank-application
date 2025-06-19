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
import ru.kalinin.deal.services.DealService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/deal", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DealControllerImpl implements DealController {
//    private final CommonProps commonProps;  // url калькулятора
    private final DealService dealService;
//    private final RestUtil restUtil;    //для общения с другими MS

    @Override
    @PostMapping("/statement")
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @RequestBody @Valid LoanStatementRequestDto requestDto)
    {

        log.info("POST request {} path {}", requestDto, "/deal/statement");

        return  ResponseEntity.ok(dealService.createStatement(requestDto));
    }

    @Override
    @PostMapping("/offer/select")
    public ResponseEntity<Void> selectStatement(LoanOfferDto requestDto) {
        dealService.selectStatement(requestDto);
        return null;
    }

    @Override
    @PostMapping("/calculate/{statementId}")
    public ResponseEntity<Void> finishRegistrationAndCalculateCredit(
            @PathVariable String statementId,
            @RequestBody FinishRegistrationRequestDto requestDto) {
        dealService.finishRegistrationAndCalculateCredit(statementId, requestDto);
        return null;
    }


}
