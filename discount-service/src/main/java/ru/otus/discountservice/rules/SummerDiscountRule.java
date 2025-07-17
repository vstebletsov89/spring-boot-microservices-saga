package ru.otus.discountservice.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.common.request.DiscountRequest;
import ru.otus.discountservice.feature.FeatureChecker;

import java.math.BigDecimal;
import java.time.Month;

@Component
@RequiredArgsConstructor
public class SummerDiscountRule implements DiscountRule {

    private static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.95); // 5% discount

    private final FeatureChecker featureChecker;

    @Override
    public boolean isApplicable(DiscountRequest request) {
        if (!featureChecker.isSummerDiscountEnabled()) return false;

        Month departureMonth = request.departureDate().getMonth();
        return departureMonth == Month.JUNE || departureMonth == Month.JULY || departureMonth == Month.AUGUST;
    }

    @Override
    public BigDecimal apply(BigDecimal currentPrice) {
        return currentPrice.multiply(DISCOUNT_RATE);
    }
}
