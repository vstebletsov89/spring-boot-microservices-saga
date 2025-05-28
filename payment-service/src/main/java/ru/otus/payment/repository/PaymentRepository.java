package ru.otus.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.payment.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByBookingId(String bookingId);
}