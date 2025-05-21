package ru.otus.payment.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.otus.common.request.PaymentRequest;
import ru.otus.common.response.PaymentResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClientAdapter {

    private final PaymentClient paymentClient;

    @Retry(name = "paymentRetry", fallbackMethod = "fallback")
    @CircuitBreaker(name = "defaultCircuitBreaker", fallbackMethod = "fallback")
    @RateLimiter(name = "RPMRateLimiter", fallbackMethod = "fallback")
    public ResponseEntity<PaymentResponse> doResilientPayment(PaymentRequest request) {
        log.info("PaymentClientAdapter: {}", request);
        return paymentClient.doPayment(request);
    }

    public ResponseEntity<PaymentResponse> fallback(PaymentRequest request, Throwable throwable) {
        var cb = CircuitBreakerRegistry.ofDefaults().circuitBreaker("defaultCircuitBreaker");
        log.info("CircuitBreaker is now: {}", cb.getState());
        throw new RuntimeException("PaymentClientAdapter fallback: " + throwable.getMessage(), throwable);
    }
}
