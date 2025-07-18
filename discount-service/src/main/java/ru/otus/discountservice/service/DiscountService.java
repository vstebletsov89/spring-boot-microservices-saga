package ru.otus.discountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.response.DiscountResponse;
import ru.otus.discountservice.rules.DiscountRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private static final int PRICE_SCALE = 2;

    private final List<DiscountRule> discountRules;

    public DiscountResponse calculateFinalPrice(DiscountRequest request) {
        BigDecimal basePrice = request.basePrice();
        BigDecimal currentPrice = basePrice;

        log.info("Calculating final price. Base price: {}", basePrice);

        for (DiscountRule rule : discountRules) {
            if (rule.isApplicable(request)) {
                BigDecimal newPrice = rule.apply(currentPrice);
                log.debug("Applied {}  price: {} -> {}", rule.getClass().getSimpleName(), currentPrice, newPrice);
                currentPrice = newPrice;
            } else {
                log.debug("Skipped {} (not applicable)", rule.getClass().getSimpleName());
            }
        }

        BigDecimal finalPrice = currentPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
        log.info("Final price after discounts: {}", finalPrice);

        return new DiscountResponse(finalPrice);
    }
}
