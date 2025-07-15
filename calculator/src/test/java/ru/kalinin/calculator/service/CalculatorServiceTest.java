package ru.kalinin.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kalinin.common.dto.*;
import ru.kalinin.common.enums.EmploymentStatus;
import ru.kalinin.common.enums.Gender;
import ru.kalinin.common.enums.MaritalStatus;
import ru.kalinin.common.enums.Position;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorServiceTest {

    private CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new CalculatorServiceImpl();
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
        emp.setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);
        emp.setEmployerINN("1234567890");
        emp.setSalary(new BigDecimal("50000"));
        emp.setPosition(Position.MID_MANAGER);
        emp.setWorkExperienceTotal(36);
        emp.setWorkExperienceCurrent(12);
        dto.setEmployment(emp);
        dto.setMaritalStatus(MaritalStatus.MARRIED);
        dto.setGender(Gender.MALE);
        dto.setIsInsuranceEnabled(true);
        dto.setIsSalaryClient(true);
        return dto;
    }

    @Test
    void calculateOffers_returnsFourOffers() {
        // Проверяет, что метод calculateOffers возвращает 4 предложения по кредиту с правильной суммой
        LoanStatementRequestDto request = getValidLoanRequest();
        List<LoanOfferDto> offers = service.calculateOffers(request);
        assertEquals(4, offers.size());
        assertTrue(offers.stream().allMatch(o -> o.getRequestedAmount().equals(request.getAmount())));
    }

    @Test
    void calculateOffers_invalidName_throwsException() {
        // Проверяет, что при некорректном (не латинском) имени выбрасывается исключение
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setFirstName("Иван"); // не латиница
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidLastName_throwsException() {
        // Проверяет, что при некорректном (не латинском) имени выбрасывается исключение
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setLastName("Иванов"); // не латиница
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidMiddleName_throwsException() {
        // Проверяет, что при некорректном (не латинском) имени выбрасывается исключение
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setMiddleName("Иванович"); // не латиница
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidAmount_throwsException() {
        // Проверяет, что при некорректной сумме кредита (менее 20000)
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setAmount(new BigDecimal(10000)); // 10000 < 20000
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidTerm_throwsException() {
        // Проверяет, что при некорректном сроке кредита (менее 6 месяцев)
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setTerm(2); // 2 месяца на кредит
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidBirthdate_throwsException() {
        // Проверяет, что при некорректном возрасте (менее 18 лет)
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setBirthdate(java.time.LocalDate.now().minusYears(17)); // 17 лет
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidEmail_throwsException() {
        // Проверяет, что при некорректном Email
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setEmail("АБВ@яндекс.ру");
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidPassportSeries_throwsException() {
        // Проверяет, что при некорректной серии паспорта
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setPassportSeries("1234567890");
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateOffers_invalidPassportNumber_throwsException() {
        // Проверяет, что при некорректной номера паспорта
        LoanStatementRequestDto request = getValidLoanRequest();
        request.setPassportNumber("1234567890");
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }

    @Test
    void calculateCredit_validData_returnsCreditDto() {
        // Проверяет, что при корректных данных метод calculateCredit возвращает корректный объект кредита
        ScoringDataDto data = getValidScoringData();
        CreditDto credit = service.calculateCredit(data);
        assertNotNull(credit);
        assertEquals(data.getAmount(), credit.getAmount());
        assertEquals(data.getTerm(), credit.getTerm());
        assertNotNull(credit.getMonthlyPayment());
        assertNotNull(credit.getRate());
        assertNotNull(credit.getPsk());
        assertNotNull(credit.getPaymentSchedule());
        assertEquals(data.getIsInsuranceEnabled(), credit.getIsInsuranceEnabled());
        assertEquals(data.getIsSalaryClient(), credit.getIsSalaryClient());
    }

    @Test
    void calculateCredit_invalidAge_throwsException() {
        // Проверяет, что при слишком маленьком возрасте клиента выбрасывается исключение
        ScoringDataDto data = getValidScoringData();
        data.setBirthdate(LocalDate.now().minusYears(17));
        assertThrows(IllegalArgumentException.class, () -> service.calculateCredit(data));
    }

    @Test
    void calculateCredit_unemployed_throwsException() {
        // Проверяет, что при статусе "безработный" выбрасывается исключение
        ScoringDataDto data = getValidScoringData();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        assertThrows(IllegalArgumentException.class, () -> service.calculateCredit(data));
    }

    @Test
    void calculateCredit_excessiveAmount_throwsException() {
        // Проверяет, что при слишком большой сумме кредита (больше 24 зарплат) выбрасывается исключение
        ScoringDataDto data = getValidScoringData();
        data.setAmount(new BigDecimal("2000000")); // больше 24 зарплат
        assertThrows(IllegalArgumentException.class, () -> service.calculateCredit(data));
    }

    @Test
    void calculateCredit_insufficientExperience_throwsException() {
        // Проверяет, что при недостаточном общем стаже работы выбрасывается исключение
        ScoringDataDto data = getValidScoringData();
        data.getEmployment().setWorkExperienceTotal(10);
        assertThrows(IllegalArgumentException.class, () -> service.calculateCredit(data));
    }
}
