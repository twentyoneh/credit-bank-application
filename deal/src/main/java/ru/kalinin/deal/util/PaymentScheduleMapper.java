package ru.kalinin.deal.util;

import org.mapstruct.Mapper;
import ru.kalinin.deal.dto.PaymentScheduleElementDto;
import ru.kalinin.deal.models.PaymentScheduleElement;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentScheduleMapper {

    List<PaymentScheduleElement> mapList(List<PaymentScheduleElementDto> paymentSchedule);

    List<PaymentScheduleElementDto> mapListToDto(List<PaymentScheduleElement> paymentSchedule);
}