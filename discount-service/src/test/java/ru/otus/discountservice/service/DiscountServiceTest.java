package ru.otus.discountservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.response.DiscountResponse;
import ru.otus.discountservice.rules.DiscountRule;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.axonframework.common.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DiscountService.class})
class DiscountServiceTest {

    @MockitoBean
    private DiscountRule rule1;

    @MockitoBean
    private DiscountRule rule2;

    @Autowired
    private DiscountService discountService;

    @Test
    void shouldApplyAllApplicableDiscountRules() {
         DiscountRequest request = new DiscountRequest(
                new BigDecimal("1000.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(50),
                true,
                10
        );


        when(rule1.isApplicable(request)).thenReturn(true);
        when(rule1.apply(new BigDecimal("1000.00"))).thenReturn(new BigDecimal("900.00"));
        when(rule2.isApplicable(request)).thenReturn(true);
        when(rule2.apply(new BigDecimal("900.00"))).thenReturn(new BigDecimal("850.00"));

        DiscountResponse result = discountService.calculateFinalPrice(request);

        assertEquals(new BigDecimal("850.00"), result.finalPrice());
    }

    @Test
    void shouldSkipNonApplicableDiscountRules() {
        DiscountRequest request = new DiscountRequest(
                new BigDecimal("500.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                false,
                0
        );

        when(rule1.isApplicable(request)).thenReturn(false);
        when(rule2.isApplicable(request)).thenReturn(false);

        DiscountResponse result = discountService.calculateFinalPrice(request);

        assertEquals(new BigDecimal("500.00"), result.finalPrice());
    }
}
