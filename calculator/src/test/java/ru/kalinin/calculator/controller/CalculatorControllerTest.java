package ru.kalinin.calculator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.kalinin.calculator.service.CalculatorService;
import ru.kalinin.calculator.service.CalculatorServiceImpl;
import ru.kalinin.common.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CalculatorControllerTest {

    private CalculatorService service;
    private CalculatorController controller;

    @BeforeEach
    void setUp() {
        service = mock(CalculatorService.class);
        controller = new CalculatorControllerImpl(service);
    }

    private LoanStatementRequestDto getValidLoanRequest() {
        LoanStatementRequestDto dto = new LoanStatementRequestDto();
        dto.setFirstName("Ivan");
        dto.setLastName("Ivanov");
        dto.setMiddleName("Ivanovich");
        dto.setAmount(new BigDecimal("30000"));
        dto.setTerm(12);
        dto.setBirthdate(LocalDate.now().minusYears(30));
        dto.setEmail("ivan@test.com");
        dto.setPassportSeries("1234");
        dto.setPassportNumber("123456");
        return dto;
    }

    private ScoringDataDto getValidScoringData() {
        ScoringDataDto dto = new ScoringDataDto();
        dto.setAmount(new BigDecimal("30000"));
        dto.setTerm(12);
        dto.setBirthdate(LocalDate.now().minusYears(30));
        EmploymentDto emp = new EmploymentDto();
        emp.setSalary(new BigDecimal("50000"));
        dto.setEmployment(emp);
        return dto;
    }

    @Test
    void getLoanOffers_success() {
        // Проверяет, что при корректном запросе возвращается список из 4 предложений и статус 200
        LoanStatementRequestDto request = getValidLoanRequest();
        List<LoanOfferDto> offers = List.of(new LoanOfferDto(), new LoanOfferDto(), new LoanOfferDto(), new LoanOfferDto());
        when(service.calculateOffers(request)).thenReturn((ResponseEntity<List<LoanOfferDto>>) offers);

        ResponseEntity<List<LoanOfferDto>> response = controller.getLoanOffers(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(4, response.getBody().size());
    }

    @Test
    void getLoanOffers_badRequest() {
        // Проверяет, что при ошибке в сервисе возвращается статус 400 и пустое тело
        LoanStatementRequestDto request = getValidLoanRequest();
        when(service.calculateOffers(request)).thenThrow(new IllegalArgumentException("Ошибка"));

        ResponseEntity<List<LoanOfferDto>> response = controller.getLoanOffers(request);
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void calculateCredit_success() {
        // Проверяет, что при корректных данных возвращается объект кредита и статус 200
        ScoringDataDto data = getValidScoringData();
        CreditDto credit = new CreditDto();
        credit.setAmount(data.getAmount());
//        when(service.calculateCredit(data)).thenReturn(credit);

        ResponseEntity<CreditDto> response = controller.calculateCredit(data);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(data.getAmount(), response.getBody().getAmount());
    }

    @Test
    void calculateCredit_badRequest() {
        // Проверяет, что при ошибке в сервисе возвращается статус 400 и пустое тело
        ScoringDataDto data = getValidScoringData();
        when(service.calculateCredit(data)).thenThrow(new IllegalArgumentException("Ошибка"));

        ResponseEntity<CreditDto> response = controller.calculateCredit(data);
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}
