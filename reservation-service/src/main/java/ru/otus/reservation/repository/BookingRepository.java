package ru.otus.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.otus.reservation.entity.BookingInfo;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingInfo, Long> {
    Optional<BookingInfo> findByBookingId(String bookingId);
}

