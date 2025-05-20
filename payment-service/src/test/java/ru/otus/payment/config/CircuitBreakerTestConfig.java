package ru.otus.payment.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CircuitBreakerTestConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
