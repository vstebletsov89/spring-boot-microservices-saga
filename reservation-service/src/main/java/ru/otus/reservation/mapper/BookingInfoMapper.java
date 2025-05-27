package ru.otus.reservation.mapper;

import org.mapstruct.Mapper;
import ru.otus.common.response.BookingDetailsResponse;
import ru.otus.reservation.entity.BookingInfo;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingInfoMapper {

    BookingDetailsResponse toResponse(BookingInfo entity);

    List<BookingDetailsResponse> toResponseList(List<BookingInfo> entities);

}
