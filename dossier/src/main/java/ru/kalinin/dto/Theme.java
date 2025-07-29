package ru.kalinin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Theme {
    FINISH_REGISTRATION("завершить регистрацию"),
    CREATE_DOCUMENTS("создать документы"),
    SEND_DOCUMENTS("отправить документы"),
    SEND_SES("отправить ses"),
    CREDIT_ISSUED("кредит выдан"),
    STATEMENT_DENIED("заявление отклонено");
    private final String title;
}