package ru.kalinin.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Сделка", description = "Создание заявки, выбор предложения, завершение регистрации и расчёт кредита")
public class DealControllerImpl implements DealController {
    private final DealService dealService;

    @Override
    @PostMapping("/statement")
    @Operation(
            summary = "Создать заявку и получить предложения",
            description = "Принимает данные клиента и заявки, валидирует и возвращает список кредитных предложений."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список предложений сформирован",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "422", description = "Заявка отклонена правилами скоринга"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные клиента и параметры запрашиваемого кредита"
            )
            @RequestBody @Valid LoanStatementRequestDto requestDto)
    {
        log.info("POST request {} path {}", requestDto, "/deal/statement");
        return dealService.createStatement(requestDto);
    }

    @Override
    @PostMapping(value = "/offer/select", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Выбрать кредитное предложение",
            description = "Фиксирует выбранное клиентом предложение и запускает дальнейшую обработку."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Предложение принято"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> selectStatement(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Выбранное клиентом предложение с идентификатором заявки"
            )
            @RequestBody @Valid LoanOfferDto requestDto) {
        log.info("POST request {} path {}", requestDto, "deal/offer/select");
        return dealService.selectStatement(requestDto);
    }

    @Override
    @PostMapping(value = "/calculate/{statementId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Завершить регистрацию и рассчитать кредит",
            description = "Принимает дополнительные данные клиента, рассчитывает кредит и обновляет статус заявки."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Кредит рассчитан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена"),
            @ApiResponse(responseCode = "502", description = "Ошибка при вызове калькулятора"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> finishRegistrationAndCalculateCredit(
            @Parameter(description = "Идентификатор заявки", required = true)
            @PathVariable String statementId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Дополнительные данные для расчёта кредита"
            )
            @RequestBody FinishRegistrationRequestDto requestDto) {
        log.info("POST request {} statementId {} path {}", requestDto, statementId, "deal/calculate/{statementId}");
        return dealService.finishRegistrationAndCalculateCredit(statementId, requestDto);
    }

}
