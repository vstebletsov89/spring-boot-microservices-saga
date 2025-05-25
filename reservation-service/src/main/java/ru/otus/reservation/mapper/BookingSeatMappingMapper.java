package ru.otus.reservation.mapper;

import org.mapstruct.Mapper;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.response.BookingDetailsResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingSeatMappingMapper {

    BookingDetailsResponse toResponse(BookingSeatMapping entity);

    List<BookingDetailsResponse> toResponseList(List<BookingSeatMapping> entities);

}
