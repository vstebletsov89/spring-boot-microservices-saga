package ru.otus.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.reservation.entity.BookingOutboxEvent;

import java.util.List;
import java.util.UUID;

public interface BookingOutboxRepository extends JpaRepository<BookingOutboxEvent, UUID> {
    List<BookingOutboxEvent> findTop50BySentFalseOrderByCreatedAtAsc();
}
