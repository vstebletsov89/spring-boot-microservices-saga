package ru.otus.discountservice.feature;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.discountservice.config.DiscountFeatureProperties;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class FeatureChecker {

    private final DiscountFeatureProperties props;
    private final MeterRegistry meterRegistry;

    private final AtomicInteger earlyBookingGauge = new AtomicInteger();
    private final AtomicInteger loyaltyGauge = new AtomicInteger();
    private final AtomicInteger studentGauge = new AtomicInteger();
    private final AtomicInteger promoCodeGauge = new AtomicInteger();
    private final AtomicInteger summerDiscountGauge = new AtomicInteger();

    @PostConstruct
    public void registerMetrics() {
        meterRegistry.gauge("discount.feature.early_booking", earlyBookingGauge);
        meterRegistry.gauge("discount.feature.loyalty", loyaltyGauge);
        meterRegistry.gauge("discount.feature.student", studentGauge);
        meterRegistry.gauge("discount.feature.promo_code", promoCodeGauge);
        meterRegistry.gauge("discount.feature.summer_discount", summerDiscountGauge);
        updateGauges();
    }

    public void updateGauges() {
        earlyBookingGauge.set(props.isEarlyBooking() ? 1 : 0);
        loyaltyGauge.set(props.isLoyalty() ? 1 : 0);
        studentGauge.set(props.isStudent() ? 1 : 0);
        promoCodeGauge.set(props.isPromoCode() ? 1 : 0);
        summerDiscountGauge.set(props.isSummerDiscount() ? 1 : 0);
    }

    public boolean isEarlyBookingEnabled() {
        return props.isEarlyBooking();
    }

    public boolean isLoyaltyEnabled() {
        return props.isLoyalty();
    }

    public boolean isStudentEnabled() {
        return props.isStudent();
    }

    public boolean isPromoCodeEnabled() {
        return props.isPromoCode();
    }

    public boolean isSummerDiscountEnabled() {
        return props.isSummerDiscount();
    }
}
