package ru.otus.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.common.entity.Flight;

public interface FlightRepository extends JpaRepository<Flight, String> {}
