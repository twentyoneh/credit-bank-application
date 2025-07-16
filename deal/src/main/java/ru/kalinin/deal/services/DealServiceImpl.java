package ru.kalinin.deal.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.kalinin.common.dto.*;
import ru.kalinin.deal.models.*;
import ru.kalinin.deal.models.enums.ApplicationStatus;
import ru.kalinin.deal.models.enums.ChangeType;
import ru.kalinin.deal.models.enums.CreditStatus;
import ru.kalinin.deal.repositories.ClientRepository;
import ru.kalinin.deal.repositories.CreditRepository;
import ru.kalinin.deal.repositories.StatementRepository;
import ru.kalinin.deal.util.ClientMapper;
import ru.kalinin.deal.util.StatementMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DealServiceImpl implements DealService {
    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final ObjectMapper objectMapper;
    private final ClientMapper clientMapper;
    private final StatementMapper statementMapper;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8080")
            .defaultHeader("Content-Type", "application/json")
            .build();

    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto request) {
        Client client = clientMapper.toClient(request);
        clientRepository.save(client);

        Statement statement = statementMapper.toStatement(client);
        statementRepository.save(statement);

        // Отправка POST запроса на /calculator/offers МС калькулятор
        List<LoanOfferDto> offers;

        try {
            offers = restClient.post()
                    .uri("/calculator/offers")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {
                    });

        }
        catch (Exception e){
            log.error("Ошибка при вызове микросервиса калькулятора /calculator/offers: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить ответ от микросервиса калькулятора", e);
        }
        if(offers == null || offers.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        log.info("Успешно полученны оферы");
        for (LoanOfferDto offer : offers) {
            offer.setStatementId(statement.getId());
        }
        return ResponseEntity.ok(offers);
    }

    /**
     * Завершает регистрацию клиента и рассчитывает финальные параметры кредита.
     *
     * @param request Выбранный оффер, который пришёл из api /deal/statement
     */
    @Override
    public ResponseEntity<Void> selectStatement(LoanOfferDto request) {
        log.info("Получен запрос на выбор предложения: {}", request);

        Statement statement = statementRepository.findById(request.getStatementId())
                .orElseThrow(() -> {
                    log.error("Заявка не найдена по id: {}", request.getStatementId());
                    return new RuntimeException("Заявка не найдена по id: " + request.getStatementId());
                });

        log.info("Найдена заявка: {}", statement.getId());

        StatusHistory statusHistory = StatusHistory.builder()
                .status(String.valueOf(ApplicationStatus.APPROVED))
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();

        statement.getStatusHistory().add(statusHistory);
        statement.setStatus(String.valueOf(ApplicationStatus.APPROVED));
        statement.setAppliedOffer(request);

        Credit credit = Credit.builder()
                .amount(request.getTotalAmount())
                .term(request.getTerm())
                .monthlyPayment(request.getMonthlyPayment())
                .rate(request.getRate())
                .insuranceEnabled(request.getIsInsuranceEnabled())
                .salaryClient(request.getIsSalaryClient())
                .build();

        try {
            creditRepository.save(credit);

            log.info("Credit успешно сохранен: {}", credit);
        } catch (Exception e) {
            log.error("Ошибка при сохранении Credit {} в базу данных: {}", credit.getId(), e.getMessage(), e);
            throw new RuntimeException("Ошибка при сохранении Credit в базу данных", e);
        }
        statement.setCredit(credit);

        try {
            statementRepository.save(statement);

            log.info("Заявка {} успешно обновлена: статус {}, выбрано предложение {}",
                    statement.getId(), statement.getStatus(), request);
        } catch (Exception e) {
            log.error("Ошибка при сохранении заявки {} в базу данных: {}", statement.getId(), e.getMessage(), e);
            throw new RuntimeException("Ошибка при сохранении заявки в базу данных", e);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Завершает регистрацию клиента по заявке и рассчитывает финальные параметры кредита.
     * Формирует объект ScoringDataDto на основе данных клиента и заявки, отправляет его в микросервис-калькулятор,
     * получает финальные параметры кредита, сохраняет их в базу и обновляет статус заявки.
     *
     * @param statementId идентификатор заявки
     * @param requestDto данные для завершения регистрации и скоринга
     */
    @Override
    public ResponseEntity<Void> finishRegistrationAndCalculateCredit(String statementId, FinishRegistrationRequestDto requestDto) {
        Statement statement = statementRepository.findById(UUID.fromString(statementId))
                .orElseThrow(() -> new RuntimeException("Заявка не найдена по id: " + statementId));
        log.info("Найдена заявка: {}", statementId);

        ScoringDataDto scoringDataDto = getScoringDataDto(requestDto, statement);

        log.info("Создана ScoringDto: {}", scoringDataDto);

        CreditDto creditDto;

        String json = "";
        try {
            json = objectMapper.writeValueAsString(scoringDataDto);
            log.info("Сериализованный объект ScoringDataDto: {}", json);
        } catch (Exception e) {
            log.error("Ошибка сериализации объекта ScoringDataDto: {}", e.getMessage());
        }

        try {
            creditDto = restClient.post()
                    .uri("/calculator/calc")
                    .body(json)
                    .retrieve()
                    .body(new ParameterizedTypeReference<CreditDto>() {
                    });

            if(creditDto == null){
                throw new RuntimeException("Ответ от микросервиса калькулятора пустой");
            }
        }
        catch (Exception e){
            log.error("Ошибка при вызове микросервиса калькулятора /calc: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить ответ от микросервиса калькулятора", e);
        }

        try {
            log.info("Создание финального кредита на основе CreditDto: {}", creditDto);
            Credit finalCredit = statement.getCredit();

            log.info("Id кредита: {}",finalCredit.getId() );
            finalCredit = Credit.builder()
                    .id(finalCredit.getId())
                    .amount(creditDto.getAmount())
                    .term(creditDto.getTerm())
                    .monthlyPayment(creditDto.getMonthlyPayment())
                    .rate(creditDto.getRate())
                    .psk(creditDto.getPsk())
                    .paymentSchedule(creditDto.getPaymentSchedule())
                    .insuranceEnabled(creditDto.getIsInsuranceEnabled())
                    .salaryClient(creditDto.getIsSalaryClient())
                    .creditStatus(CreditStatus.CALCULATED)
                    .build();

            creditRepository.save(finalCredit);
            log.info("Финальный кредит успешно сохранён: {}", finalCredit);
        } catch (Exception e) {
            log.error("Ошибка при сохранении финального кредита: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при сохранении финального кредита", e);
        }

        try {
            StatusHistory statusHistory = StatusHistory.builder()
                    .status("FinishRegistration " + requestDto + "was select")
                    .time(LocalDateTime.now())
                    .changeType(ChangeType.AUTOMATIC)
                    .build();
            statement.setStatusHistory(List.of(statusHistory));
            log.info("История статусов обновлена для заявки {}: {}", statement.getId(), statusHistory);

            statementRepository.save(statement);
            log.info("Заявка {} успешно обновлена после завершения регистрации", statement.getId());
        } catch (Exception e) {
            log.error("Ошибка при обновлении заявки {}: {}", statement.getId(), e.getMessage(), e);
            throw new RuntimeException("Ошибка при обновлении заявки после завершения регистрации", e);
        }
        return ResponseEntity.ok().build();
    }

    private static ScoringDataDto getScoringDataDto(FinishRegistrationRequestDto requestDto, Statement statement) {
        Client client = statement.getClient();
        ScoringDataDto scoringDataDto = new ScoringDataDto();
        scoringDataDto.setAmount(BigDecimal.valueOf(requestDto.getDependentAmount()));
        scoringDataDto.setTerm(statement.getCredit().getTerm());
        scoringDataDto.setFirstName(client.getFirstName());
        scoringDataDto.setLastName(client.getLastName());
        scoringDataDto.setMiddleName(client.getMiddleName());
        scoringDataDto.setGender(requestDto.getGender());
        scoringDataDto.setBirthdate(client.getBirthDate());
        scoringDataDto.setPassportSeries(client.getPassport().getSeries());
        scoringDataDto.setPassportNumber(client.getPassport().getNumber());
        scoringDataDto.setPassportIssueDate(requestDto.getPassportIssueDate());
        scoringDataDto.setPassportIssueBranch(requestDto.getPassportIssueBrach());
        scoringDataDto.setMaritalStatus(requestDto.getMaritalStatus());
        scoringDataDto.setDependentAmount(requestDto.getDependentAmount());
        scoringDataDto.setEmployment(requestDto.getEmployment());
        scoringDataDto.setAccountNumber(client.getAccountNumber());
        scoringDataDto.setIsInsuranceEnabled(statement.getCredit().getInsuranceEnabled());
        scoringDataDto.setIsSalaryClient(statement.getCredit().getSalaryClient());
        return scoringDataDto;
    }


}
