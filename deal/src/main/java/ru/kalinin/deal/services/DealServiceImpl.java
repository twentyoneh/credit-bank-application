package ru.kalinin.deal.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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

import java.math.BigDecimal;
import java.time.LocalDate;
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

//    private final ModelMapper mapper;
//    private final MessagingService messagingService;

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        Client client = new Client();
        client.setLastName(request.getLastName());
        client.setFirstName(request.getFirstName());
        client.setMiddleName(request.getMiddleName());
        client.setBirthDate(request.getBirthdate());
        client.setEmail(request.getEmail());
        client.setDependentAmount(request.getAmount());
        Passport passport = Passport.builder()
                .series(request.getPassportSeries())
                .number(request.getPassportNumber())
                .build();
        client.setPassport(passport);

        clientRepository.save(client);

        Statement statement = new Statement();
        statement.setClient(client);
        statementRepository.save(statement);

        // Отправка POST запроса на /calculator/offers МС калькулятор
        List<LoanOfferDto> offers;
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("Content-Type", "application/json")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = new String();
        try {
            json = objectMapper.writeValueAsString(request);
            log.info("Сериализованный объект LoanStatementRequestDto: {}", json);
        } catch (Exception e) {
            log.error("Ошибка сериализации объекта LoanStatementRequestDto: {}", e.getMessage());
        }

        try {
            offers = restClient.post()
                    .uri("/calculator/offers")
                    .body(json)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {
                    });

            if(offers == null || offers.isEmpty()){
                throw new RuntimeException("Ответ от микросервиса калькулятора пустой");
            }
        }
        catch (Exception e){
            log.error("Ошибка при вызове микросервиса калькулятора /calculator/offers: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить ответ от микросервиса калькулятора", e);
        }

        log.info("Успешно полученны оферы");
        for (LoanOfferDto offer : offers) {
            offer.setStatementId(statement.getId());
        }
        return offers;
    }

    @Override
    public void selectStatement(LoanOfferDto request) {
        log.info("Получен запрос на выбор предложения: {}", request);

        Statement statement = statementRepository.findById(request.getStatementId())
                .orElseThrow(() -> {
                    log.error("Заявка не найдена по id: {}", request.getStatementId());
                    return new RuntimeException("Заявка не найдена по id: " + request.getStatementId());
                });

        log.info("Найдена заявка: {}", statement.getId());

        StatusHistory statusHistory = StatusHistory.builder()
                .status("LoanOffer " + request + "was select")
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();

        statement.setStatusHistory(List.of(statusHistory));
        statement.setStatus(ApplicationStatus.APPROVED);
        statement.setAppliedOffer(request);

        try {
            statementRepository.save(statement);
            statementRepository.flush(); // Гарантирует, что изменения попадут в БД немедленно

            log.info("Заявка {} успешно обновлена: статус {}, выбрано предложение {}",
                    statement.getId(), statement.getStatus(), request);
        } catch (Exception e) {
            log.error("Ошибка при сохранении заявки {} в базу данных: {}", statement.getId(), e.getMessage(), e);
            throw new RuntimeException("Ошибка при сохранении заявки в базу данных", e);
        }
    }

    @Override
    public void finishRegistrationAndCalculateCredit(String statementId, FinishRegistrationRequestDto requestDto) {
        Statement statement = statementRepository.findById(UUID.fromString(statementId))
                .orElseThrow(() -> new RuntimeException("Заявка не найдена по id: " + statementId));

        Client client = statement.getClient();
        ScoringDataDto scoringDataDto = new ScoringDataDto();
        scoringDataDto.setAmount(BigDecimal.valueOf(requestDto.getDependentAmount()));
        scoringDataDto.setFirstName(client.getFirstName());
        scoringDataDto.setLastName(client.getLastName());
        scoringDataDto.setMiddleName(client.getMiddleName());
        scoringDataDto.setGender(requestDto.getGender());
        scoringDataDto.setBirthdate(client.getBirthDate());
        scoringDataDto.setPassportSeries(client.getPassport().getSeries());
        scoringDataDto.setPassportNumber(client.getPassport().getNumber());
        scoringDataDto.setPassportIssueDate(client.getPassport().getIssueDate());
        scoringDataDto.setPassportIssueBranch(client.getPassport().getIssueBranch());
        scoringDataDto.setMaritalStatus(requestDto.getMaritalStatus());
        scoringDataDto.setDependentAmount(requestDto.getDependentAmount());
        scoringDataDto.setEmployment(requestDto.getEmployment());
        scoringDataDto.setAccountNumber(client.getAccountNumber());


        CreditDto creditDto;
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = new String();
        try {
            json = objectMapper.writeValueAsString(scoringDataDto);
            log.info("Сериализованный объект scoringDataDto: {}", json);
        } catch (Exception e) {
            log.error("Ошибка сериализации объекта scoringDataDto: {}", e.getMessage());
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

        Credit finalCredit = Credit.builder()
                .amount(creditDto.getAmount())
                .term(creditDto.getTerm())
                .monthlyPayment(creditDto.getMonthlyPayment())
                .rate(creditDto.getRate())
                .paymentSchedule(creditDto.getPaymentSchedule())
                .insuranceEnabled(creditDto.getIsInsuranceEnabled())
                .salaryClient(creditDto.getIsSalaryClient())
                .creditStatus(CreditStatus.CALCULATED)
                .build();

        creditRepository.save(finalCredit);

        StatusHistory statusHistory = StatusHistory.builder()
                .status("FinishRegistration " + requestDto + "was select")
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
        statement.setStatusHistory(List.of(statusHistory));

        statementRepository.save(statement);
    }


}
