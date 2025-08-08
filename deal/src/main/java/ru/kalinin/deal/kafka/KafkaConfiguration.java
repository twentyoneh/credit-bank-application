package ru.kalinin.deal.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.kalinin.dossier.dto.EmailMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, EmailMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, EmailMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic finishRegistrationTopic() {
        return new NewTopic("finish-registration", 2, (short) 1);
    }

    @Bean
    public NewTopic createDocumentsTopic() {
        return new NewTopic("create-documents", 2, (short) 1);
    }

    @Bean
    public NewTopic sendDocumentsTopic() {
        return new NewTopic("send-documents", 2, (short) 1);
    }

    @Bean
    public NewTopic sendSesTopic() {
        return new NewTopic("send-ses", 2, (short) 1);
    }

    @Bean
    public NewTopic statementDeniedTopic() {
        return new NewTopic("statement-denied", 2, (short) 1);
    }

    @Bean
    public NewTopic creditIssuedTopic() {
        return new NewTopic("credit-issued", 2, (short) 1);
    }

}
