package ru.otus.discountservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "discount.features")
@Getter
@Setter
public class DiscountFeatureProperties {
    private boolean earlyBooking;
    private boolean loyalty;
    private boolean student;
    private boolean promoCode;
    private boolean summerDiscount;
}
