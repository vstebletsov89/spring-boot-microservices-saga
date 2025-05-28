package ru.otus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.command.RefundPaymentCommand;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.common.request.PaymentRequest;
import ru.otus.common.response.PaymentResponse;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;
import ru.otus.common.saga.PaymentRefundedEvent;
import ru.otus.payment.client.PaymentClient;
import ru.otus.payment.client.PaymentClientAdapter;
import ru.otus.payment.entity.Payment;
import ru.otus.payment.publisher.PaymentPublisher;
import ru.otus.payment.repository.PaymentRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final EventGateway eventGateway;

    private final PaymentPublisher paymentPublisher;

    private final PaymentRepository paymentRepository;

    private final PaymentClient paymentClient;

    private final PaymentClientAdapter paymentClientAdapter;

    @Transactional
    public void process(ProcessPaymentCommand cmd) {
        log.info("Processing payment: {}", cmd);

        // call for payment provider
        // dummy payment processed
        var response = paymentClientAdapter.doResilientPayment(
                new PaymentRequest(cmd.bookingId(), cmd.userId(), cmd.amount())
        );

        PaymentResponse paymentResponse = response.getBody();
        if (paymentResponse == null) {
            throw new RuntimeException("Payment service failed");
        }
        log.info("Payment response status: {}",  paymentResponse.status());

        if (response.getStatusCode().is2xxSuccessful()) {
            eventGateway.publish(new PaymentProcessedEvent(
                    cmd.bookingId(),
                    cmd.userId()
            ));
        } else {
            eventGateway.publish(new PaymentFailedEvent(
                    cmd.bookingId(),
                    cmd.userId(),
                    paymentResponse.failureReason()
            ));
        }

        String eventId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .eventId(eventId)
                .bookingId(cmd.bookingId())
                .userId(cmd.userId())
                .amount(cmd.amount())
                .status(paymentResponse.status())
                .failureReason(paymentResponse.failureReason())
                .occurredAt(paymentResponse.occurredAt())
                .build();
        paymentRepository.save(payment);
        log.info("Saved Payment to DB: {}", payment);

        sendPaymentToKafka(payment);
    }

    @Transactional
    public void refund(RefundPaymentCommand cmd) {
        log.info("Processing payment refund: {}", cmd);

        Payment previousPayment = paymentRepository.findByBookingId(cmd.bookingId())
                .orElseThrow(() -> new RuntimeException("No payment found for bookingId: " + cmd.bookingId()));

        // dummy refund processed
        var refundResponse = new PaymentResponse(
                previousPayment.getUserId(),
                PaymentStatus.REFUNDED,
                "",
                Instant.now());

        eventGateway.publish(new PaymentRefundedEvent(cmd.bookingId()));

        previousPayment.setStatus(refundResponse.status());
        previousPayment.setFailureReason(refundResponse.failureReason());
        previousPayment.setOccurredAt(refundResponse.occurredAt());
        paymentRepository.save(previousPayment);
        log.info("Updated Payment (refunded) in DB: {}", previousPayment);

        sendPaymentToKafka(previousPayment);
    }

    private void sendPaymentToKafka(Payment payment) {
        PaymentEvent kafkaEvent = new PaymentEvent(
                payment.getEventId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getOccurredAt()
        );
        paymentPublisher.publish(kafkaEvent.eventId(), kafkaEvent);
        log.info("Published Kafka PaymentEvent: {}", kafkaEvent);
    }
}
