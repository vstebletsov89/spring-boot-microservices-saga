package ru.otus.flightquery.mapper;

import org.mapstruct.Mapper;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.response.BookingSeatMappingResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingSeatMappingMapper {

    BookingSeatMappingResponse toResponse(BookingSeatMapping entity);

    BookingSeatMapping toEntity(BookingSeatMappingResponse response);

    List<BookingSeatMappingResponse> toResponseList(List<BookingSeatMapping> entities);

}
