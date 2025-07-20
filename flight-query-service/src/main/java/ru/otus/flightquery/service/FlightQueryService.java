package ru.otus.flightquery.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.FlightResponse;
import ru.otus.common.response.RoundTripFlightResponse;
import ru.otus.flightquery.client.DiscountServiceClient;
import ru.otus.flightquery.mapper.FlightMapper;
import ru.otus.flightquery.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightQueryService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final DiscountServiceClient discountServiceClient;

    @Cacheable(value = "roundTripFlights", key = "#request.hashCode()")
    public RoundTripFlightResponse searchRoundTripFlights(FlightSearchRequest request) {
        List<FlightResponse> outbound = flightRepository.findFlightsBetweenDates(
                        request.fromCode(),
                        request.toCode(),
                        request.departureDate(),
                        request.returnDate()
                ).stream()
                .filter(f -> f.getTotalSeats() - f.getReservedSeats() >= request.passengerCount())
                .map(flightMapper::toResponse)
                .toList();

        List<FlightResponse> back = flightRepository.findFlightsBetweenDates(
                        request.toCode(),
                        request.fromCode(),
                        request.departureDate(),
                        request.returnDate()
                ).stream()
                .filter(f -> f.getTotalSeats() - f.getReservedSeats() >= request.passengerCount())
                .map(flightMapper::toResponse)
                .toList();

        var outboundFlightsFinalPrice = applyDiscountsAsync(outbound, request);
        var backFlightsFinalPrice = applyDiscountsAsync(back, request);

        return new RoundTripFlightResponse(outboundFlightsFinalPrice, backFlightsFinalPrice);
    }

    private List<FlightResponse> applyDiscountsAsync(List<FlightResponse> flights, FlightSearchRequest req) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Map<String, BigDecimal> prices = new ConcurrentHashMap<>();

        for (FlightResponse flight : flights) {

            DiscountRequest discountRequest = new DiscountRequest(
                    flight.price(),
                    LocalDate.now(),
                    req.departureDate().toLocalDate(),
                    true, // hardcoded student flag
                    100 // hardcoded number of bookings
            );

            var future = discountServiceClient.getDiscountedPriceAsync(discountRequest)
                    .orTimeout(3, TimeUnit.SECONDS)
                    .thenAccept(discountedPrice -> prices.put(flight.flightNumber(), discountedPrice))
                    .exceptionally(ex -> {
                        log.warn("Discount for flight {} fallback to base price: {}", flight.flightNumber(), ex.toString());
                        prices.put(flight.flightNumber(), flight.price());
                        return null;
                    });

            futures.add(future);
        }

        try {

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(3, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.warn("Some discounts were not applied in time: {}", e.getMessage());
        }

        //debug info
        for (int i = 0; i < futures.size(); i++) {

            var future = futures.get(i);
            var flightNumber = flights.get(i).flightNumber();

            if (future.isCompletedExceptionally()) {
                log.debug("Future {} failed.", flightNumber);
            } else if (future.isDone()) {
                log.debug("Future {} completed.", flightNumber);
            } else {
                log.debug("Future {} in progress.", flightNumber);
            }
        }

        return flights.stream()
                .map(flight -> new FlightResponse(
                        flight.flightNumber(),
                        flight.departureAirportCode(),
                        flight.arrivalAirportCode(),
                        flight.departureTime(),
                        flight.arrivalTime(),
                        prices.getOrDefault(flight.flightNumber(), flight.price())
                ))
                .toList();
    }
}