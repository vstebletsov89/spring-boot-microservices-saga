package ru.otus.flightquery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.common.entity.Airport;

public interface AirportRepository extends JpaRepository<Airport, String> {}
