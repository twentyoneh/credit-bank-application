package ru.kalinin.deal.util;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Statement;
import ru.kalinin.deal.models.StatusHistory;
import ru.kalinin.deal.models.enums.ApplicationStatus;
import ru.kalinin.deal.models.enums.ChangeType;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface StatementMapper {
    @Mapping(target = "client", source = "client")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", expression = "java(String.valueOf(ru.kalinin.deal.models.enums.ApplicationStatus.PREAPPROVAL))")
    Statement toStatement(Client client);

    default StatusHistory mapStatusHistory() {
        return StatusHistory.builder()
                .status(String.valueOf(ApplicationStatus.PREAPPROVAL))
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
    }

    @AfterMapping
    default  void addStatusHistory(@MappingTarget Statement statement) {
        if (statement.getStatusHistory() == null) {
            statement.setStatusHistory(new ArrayList<>());
        }
        statement.getStatusHistory().add(mapStatusHistory());
    }
}
