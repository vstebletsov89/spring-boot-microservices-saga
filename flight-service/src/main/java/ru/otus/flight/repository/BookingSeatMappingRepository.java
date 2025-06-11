package ru.otus.flight.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.flight.entity.BookingSeatMapping;

import java.util.List;
import java.util.Optional;

public interface BookingSeatMappingRepository extends JpaRepository<BookingSeatMapping, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BookingSeatMapping b WHERE b.flightNumber = :fn")
    List<BookingSeatMapping> findAllByFlightNumberForUpdate(@Param("fn") String flightNumber);

    Optional<BookingSeatMapping> findByBookingId(String bookingId);
}

