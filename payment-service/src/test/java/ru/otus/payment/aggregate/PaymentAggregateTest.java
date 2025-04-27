package ru.otus.payment.aggregate;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.payment.service.PaymentService;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PaymentAggregateTest {

    private FixtureConfiguration<PaymentAggregate> fixture;
    private PaymentService paymentService;

    private final String bookingId = "booking-123";
    private final String userId = "1";
    private final BigDecimal amount = new BigDecimal("999.99");

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        fixture = new AggregateTestFixture<>(PaymentAggregate.class);
        fixture.registerInjectableResource(paymentService);
    }

    @Test
    void shouldCallPaymentServiceOnCommand() {
        ProcessPaymentCommand command = new ProcessPaymentCommand(bookingId, userId, amount);

        fixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents();

        verify(paymentService, times(1)).process(command);
    }
}