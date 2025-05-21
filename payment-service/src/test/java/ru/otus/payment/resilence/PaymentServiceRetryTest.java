package ru.otus.payment.resilence;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.response.PaymentResponse;
import ru.otus.payment.client.PaymentClient;
import ru.otus.payment.client.PaymentClientAdapter;
import ru.otus.payment.config.CircuitBreakerTestConfig;
import ru.otus.payment.publisher.PaymentPublisher;
import ru.otus.payment.repository.PaymentRepository;
import ru.otus.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration," +
                "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterMetricsAutoConfiguration," +
                "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimitersHealthIndicatorAutoConfiguration"
})
@Import(CircuitBreakerTestConfig.class)
@ImportAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        KafkaStreamsDefaultConfiguration.class
})
class PaymentServiceRetryTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentClientAdapter paymentClientAdapter;

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
    void shouldRetry3TimesAndEventuallyReturnPayment() {
        ProcessPaymentCommand cmd = new ProcessPaymentCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString(), BigDecimal.valueOf(100));
        when(paymentClient.doPayment(any()))
                .thenThrow(new RuntimeException("Test failure"))
                .thenThrow(new RuntimeException("Test failure"))
                .thenReturn(new ResponseEntity<>(new PaymentResponse("1", PaymentStatus.SUCCESS, "", Instant.now()), HttpStatus.OK));

        paymentService.process(cmd);

        verify(paymentRepository).save(argThat(p ->
                p.getStatus() == PaymentStatus.SUCCESS &&
                p.getFailureReason().isEmpty() &&
                p.getAmount().equals(cmd.amount()) &&
                p.getBookingId().equals(cmd.bookingId()) &&
                p.getUserId().equals(cmd.userId())
        ));

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("defaultCircuitBreaker");
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        assertEquals(2, cb.getMetrics().getNumberOfFailedCalls());
    }
}
