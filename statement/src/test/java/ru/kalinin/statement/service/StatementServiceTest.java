package ru.kalinin.statement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kalinin.common.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class StatementServiceTest {
    private StatementService statementService;

    @BeforeEach
    void setUp() { statementService = new StatementServiceImpl(); }

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
        // Проверяет, что при некорректном (не латинском) имени выбрасывается исключение
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setFirstName("Иван"); // не латиница
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidLastName_throwsException() {
        // Проверяет, что при некорректном (не латинском) имени выбрасывается исключение
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setLastName("Иванов"); // не латиница
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidMiddleName_throwsException() {
        // Проверяет, что при некорректном (не латинском) имени выбрасывается исключение
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setMiddleName("Иванович"); // не латиница
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidAmount_throwsException() {
        // Проверяет, что при некорректной сумме кредита (менее 20000)
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setAmount(new BigDecimal(10000)); // 10000 < 20000
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidTerm_throwsException() {
        // Проверяет, что при некорректном сроке кредита (менее 6 месяцев)
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setTerm(2); // 2 месяца на кредит
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidBirthdate_throwsException() {
        // Проверяет, что при некорректном возрасте (менее 18 лет)
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setBirthdate(java.time.LocalDate.now().minusYears(17)); // 17 лет
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidEmail_throwsException() {
        // Проверяет, что при некорректном Email
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setEmail("АБВ@яндекс.ру");
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidPassportSeries_throwsException() {
        // Проверяет, что при некорректной серии паспорта
        LoanStatementRequestDto request = getValidLoanRequest();

        request.setPassportSeries("1234567890");
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

    @Test
    void calculateOffers_invalidPassportNumber_throwsException() {
        // Проверяет, что при некорректной номера паспорта
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setPassportNumber("1234567890");
        assertThrows(IllegalArgumentException.class, () -> statementService.createStatement(request));
    }

}
