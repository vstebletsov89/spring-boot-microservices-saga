package ru.otus.orchestrator.config;

import org.axonframework.tracing.SpanFactory;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Bean
    public SpanFactory spanFactory() {
        return OpenTelemetrySpanFactory.builder().build();
    }
}