package ru.otus.seatservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.seatservice.entity.BookingSeatMapping;

public interface BookingSeatMappingRepository extends JpaRepository<BookingSeatMapping, String> {}

