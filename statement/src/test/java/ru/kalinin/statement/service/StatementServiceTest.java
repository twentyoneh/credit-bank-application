package ru.kalinin.statement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.common.dto.LoanOfferDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class StatementServiceTest {
    private RestClient restClient;
    private StatementService statementService;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        statementService = new StatementServiceImpl(RestClient.builder().baseUrl("http://deal:8080").build());
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

    @Test
    void calculateOffers_invalidName_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setFirstName("Иван");
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidLastName_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setLastName("Иванов");
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidMiddleName_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setMiddleName("Иванович");
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidAmount_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setAmount(new BigDecimal(10000));
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidTerm_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setTerm(2);
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidBirthdate_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setBirthdate(java.time.LocalDate.now().minusYears(17));
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidEmail_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setEmail("АБВ@яндекс.ру");
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidPassportSeries_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setPassportSeries("1234567890");
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void calculateOffers_invalidPassportNumber_throwsException() {
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setPassportNumber("1234567890");
        assertTrue(statementService.createStatement(request).getBody() == null || statementService.createStatement(request).getBody().isEmpty());
    }

    @Test
    void selectOffer_exceptionThrown_runtimeException() {
        LoanOfferDto offerDto = LoanOfferDto.builder().build();

        when(restClient.post().uri(anyString()).body(any()).retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Ошибка"));

        assertThrows(RuntimeException.class, () -> statementService.selectOffer(offerDto));
    }

}
