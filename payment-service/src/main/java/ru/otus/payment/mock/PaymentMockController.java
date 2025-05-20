package ru.otus.payment.mock;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.request.PaymentRequest;
import ru.otus.common.response.PaymentResponse;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/mock-payments")
public class PaymentMockController {

    private final AtomicInteger counter = new AtomicInteger();

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        int call = counter.incrementAndGet();

        PaymentStatus status;
        String failureReason = null;

        // 2/3 requests failed
        if (call % 3 != 0) {
            status = PaymentStatus.FAILED;
            failureReason = "Insufficient funds";
        } else {
            status = PaymentStatus.SUCCESS;
        }

        PaymentResponse response = new PaymentResponse(
                request.userId(),
                status,
                failureReason,
                Instant.now()
        );

        return response.status() == PaymentStatus.SUCCESS
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(400).body(response);
    }
}
