package ru.otus.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.common.entity.BookingSeatMapping;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingSeatMapping, Long> {
    Optional<BookingSeatMapping> findByBookingId(String bookingId);
}

