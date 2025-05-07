package ru.otus.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.flight.entity.BookingFailure;

public interface BookingFailureRepository extends JpaRepository<BookingFailure, Long> {

}