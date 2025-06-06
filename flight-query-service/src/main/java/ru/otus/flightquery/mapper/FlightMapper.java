package ru.otus.flightquery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.otus.common.entity.Flight;
import ru.otus.common.response.FlightResponse;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    @Mapping(source = "departureAirport.code", target = "departureAirportCode")
    @Mapping(source = "arrivalAirport.code", target = "arrivalAirportCode")
    FlightResponse toResponse(Flight flight);

    //List<FlightResponse> toResponseList(List<Flight> flights);
}
