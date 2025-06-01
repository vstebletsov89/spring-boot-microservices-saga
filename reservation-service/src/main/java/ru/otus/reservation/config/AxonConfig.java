package ru.otus.reservation.config;

import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.tracing.SpanFactory;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    public SpanFactory spanFactory() {
        return OpenTelemetrySpanFactory.builder().build();
    }
}