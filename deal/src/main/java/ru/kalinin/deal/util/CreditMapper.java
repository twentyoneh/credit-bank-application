package ru.kalinin.deal.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kalinin.deal.dto.CreditDto;
import ru.kalinin.deal.models.Credit;

@Mapper(componentModel = "spring", uses = {PaymentScheduleMapper.class})
public interface CreditMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creditStatus", constant = "CALCULATED")
    Credit toCredit(CreditDto credit);

    CreditDto toCreditDto(Credit credit);
}
