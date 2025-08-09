package ru.kalinin.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kalinin.deal.services.KafkaProducerService;

@Tag(name = "Сделка: документы", description = "API по оформлению и отправке документов по кредиту.")
@Slf4j
@RestController
@RequestMapping(path = "/deal/document/{statementId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaProducerService kafkaProducer;

    @Operation(summary = "Запрос на отправку документов.")
    @PostMapping("/send")
    public ResponseEntity<Void> sendDocOnEmail(
            @PathVariable String statementId) {
        kafkaProducer.sendDocuments(statementId);
        return null;
    }

    @Operation(summary = "Запрос на подписание документов.")
    @PostMapping("/sign")
    public ResponseEntity<Void> signDocOnEmail(
            @PathVariable String statementId) {
        kafkaProducer.signDocuments(statementId);
        return null;
    }

    @Operation(summary = "Подписание документов.")
    @PostMapping("/code")
    public ResponseEntity<Void> codeDocOnEmail(
            @PathVariable String statementId,
            @RequestBody String code) {
        kafkaProducer.verifySesCode(statementId,code);
        return null;
    }
}
