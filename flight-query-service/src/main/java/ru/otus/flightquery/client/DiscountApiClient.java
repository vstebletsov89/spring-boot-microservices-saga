package ru.otus.flightquery.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.response.DiscountResponse;
import ru.otus.flightquery.annotation.LogExecutionTime;

@RequiredArgsConstructor
@Component
public class DiscountApiClient {

    private final RestClient restClient;

    @LogExecutionTime
    public DiscountResponse getDiscount(DiscountRequest request) {

        return restClient.post()
                .uri("/api/discounts/calculate")
                .body(request)
                .retrieve()
                .body(DiscountResponse.class);
    }
}
