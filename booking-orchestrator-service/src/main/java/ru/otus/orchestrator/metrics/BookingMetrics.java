package ru.otus.orchestrator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BookingMetrics {
    private final Counter totalBookings;
    private final Counter confirmedBookings;
    private final Counter cancelledBookings;

    public BookingMetrics(MeterRegistry registry) {
        this.totalBookings = Counter.builder("booking_total").description("Total bookings").register(registry);
        this.confirmedBookings = Counter.builder("booking_confirmed_total").description("Confirmed bookings").register(registry);
        this.cancelledBookings = Counter.builder("booking_cancelled_total").description("Cancelled bookings").register(registry);
    }

    public void incrementCreated() {
        totalBookings.increment();
    }

    public void incrementConfirmed() {
        confirmedBookings.increment();
    }

    public void incrementCancelled() {
        cancelledBookings.increment();
    }
}

