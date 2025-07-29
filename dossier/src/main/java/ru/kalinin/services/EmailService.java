package ru.kalinin.services;

import ru.kalinin.dto.EmailMessageDto;

public interface EmailService {
    void sendSimpleEmail(EmailMessageDto emailMessageDto);
}
