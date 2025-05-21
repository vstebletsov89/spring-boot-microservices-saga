package ru.otus.payment.resilence;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.awaitility.Awaitility;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.payment.client.PaymentClient;
import ru.otus.payment.client.PaymentClientAdapter;
import ru.otus.payment.publisher.PaymentPublisher;
import ru.otus.payment.repository.PaymentRepository;
import ru.otus.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration," +
                "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterMetricsAutoConfiguration," +
                "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimitersHealthIndicatorAutoConfiguration," +
                "io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration," +
                "io.github.resilience4j.springboot3.retry.autoconfigure.RetryMetricsAutoConfiguration"
})
@ImportAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        KafkaStreamsDefaultConfiguration.class
})
class PaymentServiceCircuitBreakerTest {

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
    void shouldOpenCircuitBreakerAfterFailures() throws InterruptedException {
        ProcessPaymentCommand cmd = new ProcessPaymentCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString(), BigDecimal.valueOf(200));
        when(paymentClient.doPayment(any())).thenThrow(new RuntimeException("Always fail"));

        for (int i = 0; i < 12; i++) {
            try {
                paymentService.process(cmd);
            } catch (Exception ignored) {
            }
            Thread.sleep(1000);
        }

        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("defaultCircuitBreaker");
                    assertEquals(CircuitBreaker.State.OPEN, cb.getState());
                });
    }
}
