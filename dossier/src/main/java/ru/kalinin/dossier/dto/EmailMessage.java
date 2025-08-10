package ru.kalinin.dossier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kalinin.dossier.enums.Theme;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {
    private String address;
    private Theme theme;
    private String statementId;
}
