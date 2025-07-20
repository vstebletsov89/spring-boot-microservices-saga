package ru.otus.flightquery.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.response.DiscountResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceClient {

    private final RestClient discountRestClient;
    private final Executor discountExecutor;

    public CompletableFuture<BigDecimal> getDiscountedPriceAsync(DiscountRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                DiscountResponse response = discountRestClient.post()
                        .uri("/api/discounts/calculate")
                        .body(request)
                        .retrieve()
                        .body(DiscountResponse.class);

                return response.finalPrice();
            } catch (Exception ex) {
                log.error("Error calling Discount Service: {}", ex.getMessage(), ex);
                return request.basePrice();
            }
        }, discountExecutor);
    }
}
