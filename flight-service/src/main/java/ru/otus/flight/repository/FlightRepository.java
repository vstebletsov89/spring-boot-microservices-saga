package ru.otus.flight.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.common.entity.Flight;

import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.flightNumber = :fn")
    Optional<Flight> findByFlightNumberForUpdate(@Param("fn") String flightNumber);
}
