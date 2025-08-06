package ru.kalinin.dossier.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.yaml.snakeyaml.internal.Logger;
import ru.kalinin.dossier.feign.CustomErrorDecoder;

public class DealFeignClientConfiguration {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.WARNING;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
