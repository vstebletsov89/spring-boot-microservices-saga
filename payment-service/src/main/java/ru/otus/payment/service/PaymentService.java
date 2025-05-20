package ru.otus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.common.request.PaymentRequest;
import ru.otus.common.response.PaymentResponse;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;
import ru.otus.payment.client.PaymentClient;
import ru.otus.payment.entity.Payment;
import ru.otus.payment.publisher.PaymentPublisher;
import ru.otus.payment.repository.PaymentRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final EventGateway eventGateway;

    private final PaymentPublisher paymentPublisher;

    private final PaymentRepository paymentRepository;

    private final PaymentClient paymentClient;

    @Transactional
    public void process(ProcessPaymentCommand cmd) {
        log.info("Processing payment: {}", cmd);

        // call for payment provider
        // dummy payment processed
        var response = paymentClient.doPayment(
                new PaymentRequest(
                        cmd.bookingId(),
                        cmd.userId(),
                        cmd.amount()));

        PaymentResponse paymentResponse = response.getBody() != null ? response.getBody() : null;
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

        PaymentEvent kafkaEvent = new PaymentEvent(
                eventId,
                cmd.bookingId(),
                cmd.userId(),
                cmd.amount(),
                paymentResponse.status(),
                paymentResponse.failureReason(),
                paymentResponse.occurredAt()
        );
        paymentPublisher.publish(kafkaEvent.eventId(), kafkaEvent);
        log.info("Published Kafka PaymentEvent: {}", kafkaEvent);
    }
}
