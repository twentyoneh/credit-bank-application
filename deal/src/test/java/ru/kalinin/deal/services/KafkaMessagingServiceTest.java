// java
package ru.kalinin.deal.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kalinin.deal.dto.EmailMessage;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaMessagingServiceTest {

    @Mock
    private KafkaTemplate<String, EmailMessage> kafkaTemplate;

    @InjectMocks
    private KafkaMessagingService kafkaMessagingService;

    @Test
    void sendMessage_sendsWithTopicKeyAndPayload() {
        String topic = "create-documents";
        EmailMessage message = EmailMessage.builder()
                .statementId("st-1")
                .address("u@example.com")
                .build();

        // Возврат future не используется — можно не стаббить
        kafkaMessagingService.sendMessage(topic, message);

        verify(kafkaTemplate).send(eq(topic), eq("st-1"), same(message));
    }

    @Test
    void sendMessage_withNullKey_sendsWithNullKey() {
        String topic = "finish-registration";
        EmailMessage message = EmailMessage.builder()
                .statementId(null)
                .address("u@example.com")
                .build();

        kafkaMessagingService.sendMessage(topic, message);

        verify(kafkaTemplate).send(eq(topic), isNull(), same(message));
    }

    @Test
    void sendMessage_kafkaThrows_exceptionPropagates() {
        String topic = "statement-denied";
        EmailMessage message = EmailMessage.builder()
                .statementId("st-err")
                .build();

        when(kafkaTemplate.send(eq(topic), eq("st-err"), same(message)))
                .thenThrow(new RuntimeException("kafka down"));

        assertThrows(RuntimeException.class,
                () -> kafkaMessagingService.sendMessage(topic, message));
    }
}