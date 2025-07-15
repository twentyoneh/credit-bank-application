package ru.kalinin.statement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import ru.kalinin.common.dto.LoanOfferDto;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.statement.util.NotValidDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatementServiceImpl implements StatementService {
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8081")
            .defaultHeader("Content-Type", "application/json")
            .build();

    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto requestDto) {
        try {
            preScoring(requestDto);
        }
        catch (NotValidDto e) {
            log.warn("Ошибка валидации заявки: {}", e.getMessage());
            return ResponseEntity.noContent().build();
        }
        log.info("Успешно создана заявка на кредит: {}", requestDto);

        List<LoanOfferDto> offers;
        try {
            offers = restClient.post()
                    .uri("/deal/statement")
                    .body(requestDto)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {
                    });

            if(offers == null || offers.isEmpty()){
                return ResponseEntity.noContent().build();
            }
        }
        catch (Exception e){
            log.error("Ошибка при вызове микросервиса deal /deal/statement: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить ответ от микросервиса deal", e);
        }
        log.info("Получены предложения по кредиту: {}", offers);

        return ResponseEntity.ok(offers);
    }

    @Override
    public ResponseEntity<Void> selectOffer(LoanOfferDto offerDto) {
        ResponseEntity<Void> response;
        try {
            response = restClient.post()
                    .uri("/deal/offer/select")
                    .body(offerDto)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ResponseEntity<Void>>() {
                    });

        }
        catch (Exception e){
            log.error("Ошибка при вызове микросервиса deal /deal/offer/select: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить ответ от микросервиса deal", e);
        }
        log.info("Выбрано предложение по кредиту: {}", offerDto);
        return ResponseEntity.ok().build();
    }

    /**
     * Предварительная валидация данных заявки на кредит.
     * <p>
     * Проверяет корректность имени, фамилии, отчества (если указано), суммы и срока кредита,
     * даты рождения, email, серии и номера паспорта.
     * В случае некорректных данных выбрасывает IllegalArgumentException с описанием ошибки.
     *
     * @param request параметры заявки на кредит
     * @throws NotValidDto если данные не соответствуют требованиям
     */
    private void preScoring(LoanStatementRequestDto request) {
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
