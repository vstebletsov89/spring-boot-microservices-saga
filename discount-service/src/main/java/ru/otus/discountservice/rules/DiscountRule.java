package ru.otus.discountservice.rules;

import ru.otus.common.request.DiscountRequest;

import java.math.BigDecimal;

public interface DiscountRule {

    boolean isApplicable(DiscountRequest request);


    BigDecimal apply(BigDecimal currentPrice);
}
