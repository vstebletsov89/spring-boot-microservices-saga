package ru.otus.flightquery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.common.entity.Flight;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, String> {

    @Query("""
        SELECT f FROM Flight f
        WHERE f.departureAirport.code = :from
          AND f.arrivalAirport.code = :to
          AND f.departureTime BETWEEN :start AND :end
    """)
    List<Flight> findFlightsBetweenDates(String from, String to, LocalDateTime start, LocalDateTime end);
}
