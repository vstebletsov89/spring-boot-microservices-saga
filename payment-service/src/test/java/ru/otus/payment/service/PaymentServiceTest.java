package ru.otus.payment.service;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;
import ru.otus.payment.publisher.PaymentPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = PaymentService.class)
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private EventGateway eventGateway;

    @MockitoBean
    private PaymentPublisher paymentPublisher;

    @Test
    void shouldPublishPaymentProcessedEventAndKafka_whenAmountIsPositive() {
        var cmd = new ProcessPaymentCommand("b1", "10", new BigDecimal("100.00"));

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
    }

    @Test
    void shouldPublishPaymentFailedEventAndKafka_whenAmountIsZero() {
        var cmd = new ProcessPaymentCommand("b2", "20", BigDecimal.ZERO);

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
}