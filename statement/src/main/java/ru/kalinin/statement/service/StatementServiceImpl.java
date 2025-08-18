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
import ru.kalinin.statement.util.Validator;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatementServiceImpl implements StatementService {
    private final RestClient restClient;

    // /statement
    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto requestDto) {
        try {
            Validator.preScoring(requestDto);
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

    // /statement/offer
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

}
