package ru.kalinin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDto {
    private String address;
    private Theme theme;
    private UUID statementId;
}
