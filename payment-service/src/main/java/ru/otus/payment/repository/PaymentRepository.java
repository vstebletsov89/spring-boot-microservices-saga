package ru.otus.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}