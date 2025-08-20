package ru.kalinin.statement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Заявки", description = "Операции создания заявки и выбора кредитного предложения")
public class StatementControllerImpl implements StatementController {
    private final StatementService statementService;

    @Override
    @PostMapping
    @Operation(
            summary = "Создать заявку и получить предложения",
            description = "Принимает данные клиента и заявки, валидирует и возвращает список кредитных предложений."
    )
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @RequestBody @Valid LoanStatementRequestDto requestDto) {
        log.info("POST request {} path {}", requestDto, "/statement");
        return statementService.createStatement(requestDto);
    }

    @Override
    @PostMapping("/offer")
    @Operation(
            summary = "Выбрать кредитное предложение",
            description = "Фиксирует выбранное клиентом предложение по заявке и запускает дальнейшую обработку."
    )
    public ResponseEntity<Void> selectOffer(
            @RequestBody @Valid LoanOfferDto offerDto) {
        log.info("POST request {} path {}", offerDto, "/statement/offer");
        return statementService.selectOffer(offerDto);
    }


}
