package ru.otus.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.otus.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {

    @Id
    private String eventId;

    private String bookingId;

    private String userId;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String failureReason;

    private Instant occurredAt;
}
