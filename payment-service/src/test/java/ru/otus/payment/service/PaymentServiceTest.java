package ru.otus.payment.service;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.command.RefundPaymentCommand;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.common.response.PaymentResponse;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;
import ru.otus.common.saga.PaymentRefundedEvent;
import ru.otus.payment.client.PaymentClient;
import ru.otus.payment.client.PaymentClientAdapter;
import ru.otus.payment.entity.Payment;
import ru.otus.payment.publisher.PaymentPublisher;
import ru.otus.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {PaymentService.class})
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private EventGateway eventGateway;

    @MockitoBean
    private PaymentPublisher paymentPublisher;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentClient paymentClient;

    @MockitoBean
    private PaymentClientAdapter paymentClientAdapter;

    @Test
    void shouldPublishPaymentProcessedEventAndKafka_whenAmountIsPositive() {
        var cmd = new ProcessPaymentCommand("b1", "10", new BigDecimal("100.00"));
        when(paymentClientAdapter
                .doResilientPayment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(new PaymentResponse(
                                "10",
                                        PaymentStatus.SUCCESS,
                                        null,
                                        Instant.now())));

        paymentService.process(cmd);

        var processedCaptor = ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(eventGateway).publish(processedCaptor.capture());

        var axonEvent = processedCaptor.getValue();
        assertThat(axonEvent.bookingId()).isEqualTo("b1");
        assertThat(axonEvent.userId()).isEqualTo("10");

        var keyCaptor   = ArgumentCaptor.forClass(String.class);
        var kafkaCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentPublisher).publish(keyCaptor.capture(), kafkaCaptor.capture());

        var kafkaEvent = kafkaCaptor.getValue();
        assertThat(keyCaptor.getValue()).isEqualTo(kafkaEvent.eventId());
        assertThat(kafkaEvent.bookingId()).isEqualTo("b1");
        assertThat(kafkaEvent.userId()).isEqualTo("10");
        assertThat(kafkaEvent.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(kafkaEvent.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(kafkaEvent.failureReason()).isNullOrEmpty();
        assertThat(kafkaEvent.occurredAt()).isNotNull();

        paymentRepository.save(argThat(p ->
                p.getEventId().equals(kafkaEvent.eventId()) &&
                p.getAmount().equals(kafkaEvent.amount()) &&
                p.getUserId().equals(kafkaEvent.userId()) &&
                p.getStatus() == PaymentStatus.SUCCESS &&
                p.getFailureReason().equals(kafkaEvent.failureReason())
        ));
    }

    @Test
    void shouldPublishPaymentFailedEventAndKafka_whenAmountIsZero() {
        var cmd = new ProcessPaymentCommand("b2", "20", BigDecimal.ZERO);
        when(paymentClientAdapter
                .doResilientPayment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new PaymentResponse(
                                "20",
                                PaymentStatus.FAILED,
                                "Invalid payment amount",
                                Instant.now())));

        paymentService.process(cmd);

        var failedCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventGateway).publish(failedCaptor.capture());
        var axonEvent = failedCaptor.getValue();
        assertThat(axonEvent.bookingId()).isEqualTo("b2");
        assertThat(axonEvent.userId()).isEqualTo("20");
        assertThat(axonEvent.reason()).isEqualTo("Invalid payment amount");

        var keyCaptor   = ArgumentCaptor.forClass(String.class);
        var kafkaCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentPublisher).publish(keyCaptor.capture(), kafkaCaptor.capture());

        var kafkaEvent = kafkaCaptor.getValue();
        assertThat(keyCaptor.getValue()).isEqualTo(kafkaEvent.eventId());
        assertThat(kafkaEvent.bookingId()).isEqualTo("b2");
        assertThat(kafkaEvent.userId()).isEqualTo("20");
        assertThat(kafkaEvent.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(kafkaEvent.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(kafkaEvent.failureReason()).isEqualTo("Invalid payment amount");
        assertThat(kafkaEvent.occurredAt()).isNotNull();
    }

    @Test
    void shouldPublishPaymentFailedEventAndKafka_whenAmountIsNegative() {
        var cmd = new ProcessPaymentCommand("b3", "30", new BigDecimal("-50.00"));
        when(paymentClientAdapter
                .doResilientPayment(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new PaymentResponse(
                                "30",
                                PaymentStatus.FAILED,
                                "Invalid payment amount",
                                Instant.now())));

        paymentService.process(cmd);

        var failedCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventGateway).publish(failedCaptor.capture());
        var axonEvent = failedCaptor.getValue();
        assertThat(axonEvent.bookingId()).isEqualTo("b3");
        assertThat(axonEvent.userId()).isEqualTo("30");
        assertThat(axonEvent.reason()).isEqualTo("Invalid payment amount");

        var keyCaptor   = ArgumentCaptor.forClass(String.class);
        var kafkaCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentPublisher).publish(keyCaptor.capture(), kafkaCaptor.capture());

        var kafkaEvent = kafkaCaptor.getValue();
        assertThat(keyCaptor.getValue()).isEqualTo(kafkaEvent.eventId());
        assertThat(kafkaEvent.bookingId()).isEqualTo("b3");
        assertThat(kafkaEvent.userId()).isEqualTo("30");
        assertThat(kafkaEvent.amount()).isEqualByComparingTo(new BigDecimal("-50.00"));
        assertThat(kafkaEvent.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(kafkaEvent.failureReason()).isEqualTo("Invalid payment amount");
        assertThat(kafkaEvent.occurredAt()).isNotNull();
    }

    @Test
    void shouldRefundPaymentAndPublishEvents() {
        var cmd = new RefundPaymentCommand("b1");
        var payment = Payment.builder()
                .eventId("event-1")
                .bookingId("b1")
                .userId("10")
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCESS)
                .failureReason(null)
                .occurredAt(Instant.now())
                .build();
        when(paymentRepository.findByBookingId("b1")).thenReturn(Optional.of(payment));

        paymentService.refund(cmd);

        var eventCaptor = ArgumentCaptor.forClass(PaymentRefundedEvent.class);
        verify(eventGateway).publish(eventCaptor.capture());
        var event = eventCaptor.getValue();
        assertThat(event.bookingId()).isEqualTo("b1");

        verify(paymentRepository).save(payment);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getFailureReason()).isEmpty();
        assertThat(payment.getOccurredAt()).isNotNull();

        var keyCaptor   = ArgumentCaptor.forClass(String.class);
        var kafkaCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentPublisher).publish(keyCaptor.capture(), kafkaCaptor.capture());
        var kafkaEvent = kafkaCaptor.getValue();
        assertThat(keyCaptor.getValue()).isEqualTo(kafkaEvent.eventId());
        assertThat(kafkaEvent.bookingId()).isEqualTo("b1");
        assertThat(kafkaEvent.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(kafkaEvent.failureReason()).isEmpty();
        assertThat(kafkaEvent.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(kafkaEvent.occurredAt()).isNotNull();
    }

    @Test
    void shouldThrowIfPaymentNotFound() {
        var cmd = new RefundPaymentCommand("not-found");
        when(paymentRepository.findByBookingId("not-found")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refund(cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No payment found for bookingId: not-found");

        verify(eventGateway, never()).publish(Optional.ofNullable(any()));
        verify(paymentRepository, never()).save(any());
        verify(paymentPublisher, never()).publish(any(), any());
    }
}