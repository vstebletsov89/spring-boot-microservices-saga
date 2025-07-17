package ru.otus.discountservice.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.common.request.DiscountRequest;
import ru.otus.discountservice.feature.FeatureChecker;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class EarlyBookingRule implements DiscountRule {

    private static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.90);
    private static final int DAYS_THRESHOLD = 30;

    private final FeatureChecker featureChecker;

    @Override
    public boolean isApplicable(DiscountRequest request) {
        if (!featureChecker.isEarlyBookingEnabled()) return false;

        long daysBetween = ChronoUnit.DAYS.between(request.bookingDate(), request.departureDate());
        return daysBetween > DAYS_THRESHOLD;
    }

    @Override
    public BigDecimal apply(BigDecimal currentPrice) {
        return currentPrice.multiply(DISCOUNT_RATE);
    }
}
