package ru.otus.payment.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.common.request.PaymentRequest;
import ru.otus.common.response.PaymentResponse;

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
        return restClient.post()
                .body(request)
                .retrieve()
                .toEntity(PaymentResponse.class);
    }
}