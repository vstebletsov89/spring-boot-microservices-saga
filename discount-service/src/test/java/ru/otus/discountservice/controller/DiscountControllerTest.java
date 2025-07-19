package ru.otus.discountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.response.DiscountResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCalculateFinalPriceWithMultipleApplicableDiscounts() throws Exception {
        DiscountRequest request = new DiscountRequest(
                new BigDecimal("1000.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(40),
                true,
                100
        );

        System.out.println("Registered Jackson modules: " + objectMapper.getRegisteredModuleIds());

        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = mockMvc.perform(post("/api/discounts/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        DiscountResponse response = objectMapper.readValue(responseJson, DiscountResponse.class);
        BigDecimal result = response.finalPrice();

        assertThat(result)
                .isGreaterThan(BigDecimal.valueOf(0))
                .isLessThan(BigDecimal.valueOf(1000));
    }

    @Test
    void shouldReturnSamePriceIfNoDiscountsApply() throws Exception {
        DiscountRequest request = new DiscountRequest(
                new BigDecimal("500.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                false,
                0
        );

        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = mockMvc.perform(post("/api/discounts/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        DiscountResponse response = objectMapper.readValue(responseJson, DiscountResponse.class);

        assertThat(response.finalPrice()).isEqualByComparingTo("500.00");
    }
}
