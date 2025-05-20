package ru.otus.payment.resilence;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.awaitility.Awaitility;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.response.PaymentResponse;
import ru.otus.payment.client.PaymentClient;
import ru.otus.payment.config.CircuitBreakerTestConfig;
import ru.otus.payment.publisher.PaymentPublisher;
import ru.otus.payment.repository.PaymentRepository;
import ru.otus.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = PaymentService.class)
@Import(CircuitBreakerTestConfig.class)
class PaymentServiceResilienceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private EventGateway eventGateway;

    @MockitoBean
    private PaymentPublisher paymentPublisher;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentClient paymentClient;

    @Test
    void shouldRetryAndEventuallyFail() {
        ProcessPaymentCommand cmd = new ProcessPaymentCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString(), BigDecimal.valueOf(100));
        when(paymentClient.doPayment(any())).thenThrow(new RuntimeException("Test failure"));

        assertThrows(RuntimeException.class, () -> paymentService.process(cmd));

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("defaultCircuitBreaker");
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        assertEquals(1, cb.getMetrics().getNumberOfFailedCalls());
    }

    @Test
    void shouldOpenCircuitBreakerAfterFailures() {
        ProcessPaymentCommand cmd = new ProcessPaymentCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString(), BigDecimal.valueOf(200));
        when(paymentClient.doPayment(any())).thenThrow(new RuntimeException("Boom"));

        for (int i = 0; i < 6; i++) {
            try {
                paymentService.process(cmd);
            } catch (Exception ignored) {}
        }

        Awaitility.await().atMost(2, TimeUnit.SECONDS)
                .until(() -> circuitBreakerRegistry.circuitBreaker("defaultCircuitBreaker").getState() == CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldThrottleCallsWithRateLimiter() {
        ProcessPaymentCommand cmd = new ProcessPaymentCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString(), BigDecimal.valueOf(150));
        when(paymentClient.
                doPayment(any()))
                .thenReturn(new ResponseEntity<>(
                        new PaymentResponse("1", PaymentStatus.SUCCESS, "", Instant.now()), HttpStatus.OK));

        int failures = 0;
        for (int i = 0; i < 35; i++) {
            try {
                paymentService.process(cmd);
            } catch (Exception ex) {
                if (ex.getMessage().contains("RateLimiter")) {
                    failures++;
                }
            }
        }

        assertTrue(failures > 0, "Some calls should be blocked by RateLimiter");
    }
}
