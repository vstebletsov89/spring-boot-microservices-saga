package ru.otus.seatservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.seatservice.entity.SeatInventory;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, String> {}
