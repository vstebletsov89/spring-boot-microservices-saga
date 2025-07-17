package ru.otus.discountservice.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.common.request.DiscountRequest;
import ru.otus.discountservice.feature.FeatureChecker;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LoyaltyRule implements DiscountRule {

    private static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.95);
    private static final int MIN_BOOKINGS_REQUIRED = 50;

    private final FeatureChecker featureChecker;

    @Override
    public boolean isApplicable(DiscountRequest request) {
        if (!featureChecker.isLoyaltyEnabled()) return false;

        return request.completedBookings() >= MIN_BOOKINGS_REQUIRED;
    }

    @Override
    public BigDecimal apply(BigDecimal currentPrice) {
        return currentPrice.multiply(DISCOUNT_RATE);
    }
}
