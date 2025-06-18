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
import ru.kalinin.deal.config.CommonProps;
import ru.kalinin.deal.services.DealService;
import ru.kalinin.deal.util.RestUtil;

import java.beans.Statement;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/deal", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DealControllerImpl implements DealController {
    private final CommonProps commonProps;  // url калькулятора
    private final DealService dealService;
//    private final RestUtil restUtil;    //для общения с другими MS

    @Override
    @PostMapping("/statement")
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @RequestBody @Valid LoanStatementRequestDto requestDto)
    {

        log.info("POST request {} path {}", requestDto, "/deal/statement");

        Statement statement = dealService.saveStatement(requestDto);
    }

    @Override
    @PostMapping("/offer/select")
    public ResponseEntity<Void> selectStatement(LoanOfferDto requestDto) {
        return null;
    }

    @Override
    @PostMapping("/calculate/{statementId}")
    public ResponseEntity<Void> finishRegistrationAndCalculateCredit(
            @PathVariable String statementId,
            @RequestBody FinishRegistrationRequestDto requestDto) {
        return null;
    }


}
