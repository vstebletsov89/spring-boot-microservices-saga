package ru.otus.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Round-trip flight response containing lists of outbound and return flights")
public record RoundTripFlightResponse(

        @Schema(
                description = "List of outbound flights (from departure airport to destination)",
                example = """
                [
                  {
                    "flightNumber": "SU1234",
                    "departureAirportCode": "SVO",
                    "arrivalAirportCode": "DXB",
                    "departureTime": "2025-06-10T10:30:00",
                    "arrivalTime": "2025-06-10T16:20:00",
                    "price": 499.99
                  }
                ]
                """
        )
        List<FlightResponse> outboundFlights,

        @Schema(
                description = "List of return flights (from destination back to departure airport)",
                example = """
                [
                  {
                    "flightNumber": "SU1235",
                    "departureAirportCode": "DXB",
                    "arrivalAirportCode": "SVO",
                    "departureTime": "2025-06-20T13:00:00",
                    "arrivalTime": "2025-06-21T05:15:00",
                    "price": 520.00
                  }
                ]
                """
        )
        List<FlightResponse> returnFlights
) {}
