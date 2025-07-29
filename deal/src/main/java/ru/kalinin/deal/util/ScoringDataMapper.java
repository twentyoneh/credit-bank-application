package ru.kalinin.deal.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kalinin.common.dto.FinishRegistrationRequestDto;
import ru.kalinin.common.dto.ScoringDataDto;
import ru.kalinin.deal.models.Client;
import ru.kalinin.deal.models.Statement;

@Mapper(componentModel = "spring")
public interface ScoringDataMapper {

    @Mapping(target = "amount", expression = "java(java.math.BigDecimal.valueOf(requestDto.getDependentAmount()))")
    @Mapping(target = "term", source = "statement.credit.term")
    @Mapping(target = "firstName", source = "client.firstName")
    @Mapping(target = "lastName", source = "client.lastName")
    @Mapping(target = "middleName", source = "client.middleName")
    @Mapping(target = "gender", source = "requestDto.gender")
    @Mapping(target = "birthdate", source = "client.birthDate")
    @Mapping(target = "passportSeries", source = "client.passport.series")
    @Mapping(target = "passportNumber", source = "client.passport.number")
    @Mapping(target = "passportIssueDate", source = "requestDto.passportIssueDate")
    @Mapping(target = "passportIssueBranch", source = "requestDto.passportIssueBrach")
    @Mapping(target = "maritalStatus", source = "requestDto.maritalStatus")
    @Mapping(target = "dependentAmount", source = "requestDto.dependentAmount")
    @Mapping(target = "employment", source = "requestDto.employment")
    @Mapping(target = "accountNumber", source = "client.accountNumber")
    @Mapping(target = "isInsuranceEnabled", source = "statement.credit.insuranceEnabled")
    @Mapping(target = "isSalaryClient", source = "statement.credit.salaryClient")
    ScoringDataDto toScoringDataDto(Client client, Statement statement, FinishRegistrationRequestDto requestDto);
}
