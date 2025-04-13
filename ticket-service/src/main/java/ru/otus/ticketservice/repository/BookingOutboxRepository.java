package ru.otus.ticketservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.ticketservice.entity.BookingOutboxEvent;

import java.util.List;
import java.util.UUID;

public interface BookingOutboxRepository extends JpaRepository<BookingOutboxEvent, UUID> {
    List<BookingOutboxEvent> findTop50BySentFalseOrderByCreatedAtAsc();
}
