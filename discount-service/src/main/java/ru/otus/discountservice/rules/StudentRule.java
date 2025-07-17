package ru.otus.discountservice.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.common.request.DiscountRequest;
import ru.otus.discountservice.feature.FeatureChecker;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class StudentRule implements DiscountRule {

    private static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.85); // 15% discount

    private final FeatureChecker featureChecker;

    @Override
    public boolean isApplicable(DiscountRequest request) {
        if (!featureChecker.isStudentEnabled()) return false;

        return request.isStudent();
    }

    @Override
    public BigDecimal apply(BigDecimal currentPrice) {
        return currentPrice.multiply(DISCOUNT_RATE);
    }
}
