package ru.otus.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.flight.entity.Airport;

public interface AirportRepository extends JpaRepository<Airport, String> {}
