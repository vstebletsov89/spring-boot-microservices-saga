package ru.otus.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.flight.entity.BookingSeatMapping;

import java.util.List;
import java.util.Optional;

public interface BookingSeatMappingRepository extends JpaRepository<BookingSeatMapping, Long> {
    List<BookingSeatMapping> findAllByFlightNumber(String flightNumber);

    Optional<BookingSeatMapping> findByBookingId(String bookingId);
}

