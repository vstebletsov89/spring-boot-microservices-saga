package ru.otus.payment.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.request.PaymentRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentMockController.class)
class PaymentMockControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String bookingId;
    private String userId;
    private BigDecimal amount;

    @BeforeEach
    void setup() {
        bookingId = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        amount = new BigDecimal("100.00");
    }

    private String toJson(String bookingId, String userId, BigDecimal amount) throws Exception {
        return objectMapper.writeValueAsString(
                new PaymentRequest(bookingId, userId, amount)
        );
    }

    @Test
    void testMockCalls() throws Exception {
        // fist call should fail
        mockMvc.perform(post("/mock-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(bookingId, userId, amount)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(PaymentStatus.FAILED.name()))
                .andExpect(jsonPath("$.failureReason").value("Insufficient funds"));

        // second call should be OK
        mockMvc.perform(post("/mock-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(bookingId, userId, amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(PaymentStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.failureReason").doesNotExist());
    }
}