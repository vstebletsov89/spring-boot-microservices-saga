package ru.otus.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.common.request.PaymentRequest;
import ru.otus.common.response.PaymentResponse;

@Slf4j
@Component
public class PaymentClient {
    private static final String PAYMENT_URL = "http://localhost:8080/mock-payments";

    private final RestClient restClient;

    public PaymentClient() {
        this.restClient = RestClient.builder()
                .baseUrl(PAYMENT_URL)
                .build();
    }

    public ResponseEntity<PaymentResponse> doPayment(PaymentRequest request) {
        log.info("PaymentClient: {}", request);
        return restClient.post()
                .body(request)
                .retrieve()
                .toEntity(PaymentResponse.class);
    }
}