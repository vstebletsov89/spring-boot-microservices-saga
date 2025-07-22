package ru.otus.flightquery.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.response.DiscountResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceClientTest {

    @Mock
    private DiscountApiClient apiClient;

    private DiscountServiceClient discountServiceClient;

    @BeforeEach
    void setup() {
        discountServiceClient = new DiscountServiceClient(apiClient, Runnable::run);
    }

    @Test
    void shouldReturnDiscountedPrice() {
        DiscountRequest req = new DiscountRequest(
                new BigDecimal("100"),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                true,
                100
        );
        DiscountResponse res = new DiscountResponse(new BigDecimal("90"));

        when(apiClient.getDiscount(req)).thenReturn(res);

        CompletableFuture<BigDecimal> result = discountServiceClient.getDiscountedPriceAsync(req);

        assertThat(result).isCompletedWithValue(new BigDecimal("90"));
    }

    @Test
    void shouldReturnBasePriceOnException() {
        DiscountRequest req = new DiscountRequest(
                new BigDecimal("100"),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                true,
                100
        );

        when(apiClient.getDiscount(req)).thenThrow(new RuntimeException("Service unavailable"));

        CompletableFuture<BigDecimal> result = discountServiceClient.getDiscountedPriceAsync(req);

        assertThat(result).isCompletedWithValue(new BigDecimal("100"));
    }
}