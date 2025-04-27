package ru.otus.payment.service;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = PaymentService.class)
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private EventGateway eventGateway;

    @Test
    void shouldPublishPaymentProcessedEvent_whenAmountIsPositive() {
        var command = new ProcessPaymentCommand("booking-1", "10", new BigDecimal("100.00"));

        paymentService.process(command);

        ArgumentCaptor<PaymentProcessedEvent> captor = ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(eventGateway).publish(captor.capture());

        var event = captor.getValue();
        assertThat(event.bookingId()).isEqualTo("booking-1");
        assertThat(event.userId()).isEqualTo("10");
    }

    @Test
    void shouldPublishPaymentFailedEvent_whenAmountIsZero() {
        var command = new ProcessPaymentCommand("booking-2", "20", BigDecimal.ZERO);

        paymentService.process(command);

        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventGateway).publish(captor.capture());

        var event = captor.getValue();
        assertThat(event.bookingId()).isEqualTo("booking-2");
        assertThat(event.userId()).isEqualTo("20");
        assertThat(event.reason()).isEqualTo("Invalid payment amount");
    }

    @Test
    void shouldPublishPaymentFailedEvent_whenAmountIsNegative() {
        var command = new ProcessPaymentCommand("booking-3", "30", new BigDecimal("-50.00"));

        paymentService.process(command);

        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventGateway).publish(captor.capture());

        var event = captor.getValue();
        assertThat(event.bookingId()).isEqualTo("booking-3");
        assertThat(event.userId()).isEqualTo("30");
        assertThat(event.reason()).isEqualTo("Invalid payment amount");
    }
}