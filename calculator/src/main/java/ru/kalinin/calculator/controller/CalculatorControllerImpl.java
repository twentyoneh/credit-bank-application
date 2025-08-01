package ru.kalinin.calculator.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.calculator.service.CalculatorService;

import ru.kalinin.common.dto.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/calculator")
public class CalculatorControllerImpl implements CalculatorController {
    private final CalculatorService calculationService;

    public CalculatorControllerImpl(CalculatorService service) {
        this.calculationService = service;
    }
    /**
     * Возвращает список возможных кредитных предложений на основе введённых пользователем данных.
     * <p>
     * Принимает параметры заявки на кредит и рассчитывает 4 варианта кредитных предложений с разными условиями.
     *
     * @param request Данные заявки на кредит
     * @return Список кредитных предложений
     */
    @Tag(name = "getLoanOffers", description = "Возвращает список возможных кредитных предложений на основе введённых пользователем данных")
    @ApiResponse(responseCode = "200", description = "Успешный запрос — возвращён список кредитных предложений")
    @ApiResponse(responseCode = "400", description = "Неверный запрос — некорректные данные в LoanStatementRequestDto")
    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDto>> getLoanOffers(@RequestBody LoanStatementRequestDto request) {
        log.info("POST request {} path {}", request, "/calculator/offers");
        return calculationService.calculateOffers(request);
    }

    /**
     * Рассчитывает параметры кредита на основе предоставленных данных скоринга.
     *
     * @param data Данные для скоринга кредита
     * @return Рассчитанный кредит
     */
    @Tag(name = "calculateCredit", description = "Рассчитывает параметры кредита на основе предоставленных данных скоринга")
    @ApiResponse(responseCode = "200", description = "Успешный запрос — возвращает рассчитанный кредит")
    @ApiResponse(responseCode = "400", description = "Неверный запрос — некорректные данные в ScoringDataDto")
    @PostMapping("/calc")
    public ResponseEntity<CreditDto> calculateCredit(@RequestBody ScoringDataDto data) {
        return calculationService.calculateCredit(data);
    }
}
