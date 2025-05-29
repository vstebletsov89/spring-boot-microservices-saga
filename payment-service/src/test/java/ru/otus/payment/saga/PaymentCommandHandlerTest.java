package ru.otus.payment.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.command.RefundPaymentCommand;
import ru.otus.payment.service.PaymentService;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class PaymentCommandHandlerTest {

    private PaymentService paymentService;
    private PaymentCommandHandler paymentCommandHandler;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        paymentCommandHandler = new PaymentCommandHandler(paymentService);
    }

    @Test
    void shouldHandleProcessPaymentCommand() {
        var command = new ProcessPaymentCommand("booking-123", "1", BigDecimal.valueOf(500));

        paymentCommandHandler.handle(command);

        verify(paymentService).process(command);
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    void shouldHandleRefundPaymentCommand() {
        var command = new RefundPaymentCommand("booking-456");

        paymentCommandHandler.handle(command);

        verify(paymentService).refund(command);
        verifyNoMoreInteractions(paymentService);
    }
}