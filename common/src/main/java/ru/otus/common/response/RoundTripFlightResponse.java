package ru.otus.common.response;

import java.util.List;

public record RoundTripFlightResponse(
        List<FlightResponse> outboundFlights,
        List<FlightResponse> returnFlights
) {}
