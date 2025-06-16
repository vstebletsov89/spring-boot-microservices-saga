package ru.otus.payment.resilence;

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
import ru.otus.common.kafka.PaymentEvent;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(CircuitBreakerTestConfig.class)
@ImportAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        KafkaStreamsDefaultConfiguration.class
})
class PaymentServiceRateLimiterTestTest {

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
    void shouldThrottleCallsWithRateLimiter() {
        ProcessPaymentCommand cmd = new ProcessPaymentCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString(), BigDecimal.valueOf(150));
        when(paymentClient.
                doPayment(any()))
                .thenReturn(new ResponseEntity<>(
                        new PaymentResponse("1", PaymentStatus.SUCCESS, "", Instant.now()), HttpStatus.OK));

        for (int i = 0; i < 35; i++) {
            paymentService.process(cmd);
        }

        verify(paymentPublisher, atLeastOnce()).publish(
                anyString(),
                argThat(event ->
                        event instanceof PaymentEvent &&
                                ((PaymentEvent)event).status() == PaymentStatus.FAILED &&
                                ((PaymentEvent)event).failureReason().equals("The service is temporarily unavailable. Please try again later.")
                )
        );
    }
}
