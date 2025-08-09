package ru.kalinin.deal.util;

import org.mapstruct.Mapper;
import ru.kalinin.deal.dto.StatementDto;
import ru.kalinin.deal.models.Statement;

@Mapper(componentModel = "spring", uses = {CreditMapper.class})
public interface StatementMapper {
    StatementDto toStatementDto(Statement statement);
}
