package ru.otus.orchestrator.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BookingMetricsTest {

    private BookingMetrics bookingMetrics;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        bookingMetrics = new BookingMetrics(meterRegistry);
    }

    @Test
    void shouldIncrementTotalBookings() {
        bookingMetrics.incrementCreated();
        bookingMetrics.incrementCreated();

        double count = meterRegistry.get("booking_total").counter().count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void shouldIncrementConfirmedBookings() {
        bookingMetrics.incrementConfirmed();

        double count = meterRegistry.get("booking_confirmed_total").counter().count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementCancelledBookings() {
        bookingMetrics.incrementCancelled();
        bookingMetrics.incrementCancelled();
        bookingMetrics.incrementCancelled();

        double count = meterRegistry.get("booking_cancelled_total").counter().count();
        assertThat(count).isEqualTo(3.0);
    }
}