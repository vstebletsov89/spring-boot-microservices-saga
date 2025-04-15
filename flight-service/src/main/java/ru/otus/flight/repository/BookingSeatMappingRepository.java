package ru.otus.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.flight.entity.BookingSeatMapping;

public interface BookingSeatMappingRepository extends JpaRepository<BookingSeatMapping, Long> {}

