package ru.kalinin.statement.util;

import ru.kalinin.common.dto.LoanStatementRequestDto;

public class Validator {
    public static void preScoring(LoanStatementRequestDto request) {
        // Проверка имени (только латинские буквы, 2-30 символов)
        if (request.getFirstName() == null || !request.getFirstName().matches("^[A-Za-z]{2,30}$")) {
            throw new NotValidDto("Имя должно содержать от 2 до 30 латинских букв");
        }
        // Проверка фамилии (только латинские буквы, 2-30 символов)
        if (request.getFirstName() == null || !request.getLastName().matches("^[A-Za-z]{2,30}$")) {
            throw new NotValidDto("Фамилия должна содержать от 2 до 30 латинских букв");
        }
        // Проверка отчества (если указано, только латинские буквы, 2-30 символов)
        if (request.getMiddleName() != null && !request.getMiddleName().isEmpty()) {
            if (!request.getMiddleName().matches("^[A-Za-z]{2,30}$")) {
                throw new NotValidDto("Отчество должно содержать от 2 до 30 латинских букв");
            }
        }
        // Проверка суммы кредита (не менее 20000)
        if (request.getAmount() == null || request.getAmount().compareTo(new java.math.BigDecimal("20000")) < 0) {
            throw new NotValidDto("Сумма кредита должна быть не менее 20000");
        }
        // Проверка срока кредита (не менее 6 месяцев)
        if (request.getTerm() == null || request.getTerm() < 6) {
            throw new NotValidDto("Срок кредита должен быть не менее 6 месяцев");
        }
        // Проверка даты рождения (возраст не моложе 18 лет)
        if (request.getBirthdate() == null ||
                request.getBirthdate().isAfter(java.time.LocalDate.now().minusYears(18))) {
            throw new NotValidDto("Возраст должен быть не менее 18 лет");
        }
        // Проверка email на корректность формата
        if (request.getEmail() == null ||
                !request.getEmail().matches("^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$")) {
            throw new NotValidDto("Некорректный email");
        }
        // Проверка серии паспорта (4 цифры)
        if (request.getPassportSeries() == null || !request.getPassportSeries().matches("^\\d{4}$")) {
            throw new NotValidDto("Серия паспорта должна содержать 4 цифры");
        }
        // Проверка номера паспорта (6 цифр)
        if (request.getPassportNumber() == null || !request.getPassportNumber().matches("^\\d{6}$")) {
            throw new NotValidDto("Номер паспорта должен содержать 6 цифр");
        }
    }
}
