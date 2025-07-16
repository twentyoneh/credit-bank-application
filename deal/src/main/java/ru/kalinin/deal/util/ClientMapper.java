package ru.kalinin.deal.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.kalinin.common.dto.LoanStatementRequestDto;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Passport;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "passport", source = "request", qualifiedByName = "mapPassport")
    @Mapping(target = "dependentAmount", source = "amount")
    @Mapping(target = "birthDate", source = "birthdate")
    Client toClient(LoanStatementRequestDto request);

    @Named("mapPassport")
    default Passport mapPassport(LoanStatementRequestDto request) {
        return Passport.builder()
                .id(UUID.randomUUID())
                .series(request.getPassportSeries())
                .number(request.getPassportNumber())
                .build();
    }
}
