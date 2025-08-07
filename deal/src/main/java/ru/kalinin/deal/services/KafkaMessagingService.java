package ru.kalinin.deal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kalinin.deal.dto.EmailMessage;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaMessagingService {

    private final KafkaTemplate<String , EmailMessage> kafkaTemplate;


    public void sendMessage(String sendClientTopic, EmailMessage emailMessage) {
        log.info("Send message to kafka = {}", emailMessage);
        kafkaTemplate.send(sendClientTopic, emailMessage.getStatementId(), emailMessage);
        log.info("Message sent to kafka topic = {}", sendClientTopic);
    }
}
