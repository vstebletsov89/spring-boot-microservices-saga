package ru.otus.orchestrator.config;

import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.orchestrator.saga.BookingAggregate;

@Configuration
public class BookingMetricsConfig {
    @Bean
    public MeterBinder bookingMetricsBinder() {
        return BookingAggregate::registerMetrics;
    }
}
