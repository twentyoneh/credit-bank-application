package ru.kalinin.deal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deal")
@Data
public class CommonProps {
    private String calculatorUrl = "http://calculator:8080";
}
