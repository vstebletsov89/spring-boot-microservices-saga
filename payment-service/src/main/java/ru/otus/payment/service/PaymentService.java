package ru.otus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;
import ru.otus.payment.entity.Payment;
import ru.otus.payment.publisher.PaymentPublisher;
import ru.otus.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final EventGateway eventGateway;

    private final PaymentPublisher paymentPublisher;

    private final PaymentRepository paymentRepository;

    @Transactional
    public void process(ProcessPaymentCommand cmd) {
        log.info("Processing payment: {}", cmd);

        String failureReason = "";
        PaymentStatus status;

        if (cmd.amount().compareTo(BigDecimal.ZERO) <= 0) {
            status = PaymentStatus.FAILED;
            failureReason = "Invalid payment amount";
            eventGateway.publish(new PaymentFailedEvent(
                    cmd.bookingId(),
                    cmd.userId(),
                    "Invalid payment amount"
            ));
        } else {
            // call for payment provider
            // dummy payment processed
            status = PaymentStatus.SUCCESS;
            eventGateway.publish(new PaymentProcessedEvent(
                    cmd.bookingId(),
                    cmd.userId()
            ));
        }

        String eventId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.now();

        PaymentEvent kafkaEvent = new PaymentEvent(
                UUID.randomUUID().toString(),
                cmd.bookingId(),
                cmd.userId(),
                cmd.amount(),
                status,
                failureReason,
                occurredAt
        );

        Payment payment = Payment.builder()
                .eventId(eventId)
                .bookingId(cmd.bookingId())
                .userId(cmd.userId())
                .amount(cmd.amount())
                .status(status)
                .failureReason(failureReason)
                .occurredAt(occurredAt)
                .build();

        paymentRepository.save(payment);
        log.info("Saved Payment to DB: {}", payment);

        paymentPublisher.publish(kafkaEvent.eventId(), kafkaEvent);
        log.info("Published Kafka PaymentEvent: {}", kafkaEvent);
    }
}
