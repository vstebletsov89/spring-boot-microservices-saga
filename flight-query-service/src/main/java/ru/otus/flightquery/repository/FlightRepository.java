package ru.otus.flightquery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.flightquery.entity.Flight;

import java.time.ZonedDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, String> {

    @Query("""
        SELECT f FROM Flight f
        WHERE f.departureAirport.city = :fromCity
          AND f.arrivalAirport.city = :toCity
          AND f.departureTime >= :departureDate
          AND f.departureTime <= :returnDate
    """)
    List<Flight> searchFlights(
            @Param("fromCity") String fromCity,
            @Param("toCity") String toCity,
            @Param("departureDate") ZonedDateTime departureDate,
            @Param("returnDate") ZonedDateTime returnDate);
}
