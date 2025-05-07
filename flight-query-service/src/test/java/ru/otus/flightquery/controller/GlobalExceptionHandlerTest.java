package ru.otus.flightquery.controller;

import org.apache.kafka.streams.StreamsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestExceptionController.class)
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StreamsBuilder streamsBuilder;

    @Test
    void shouldReturn404WhenMessageContainsNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Resource not found"));
    }

    @Test
    void shouldReturn500OnGenericRuntimeException() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal server error: Something went wrong"));
    }

    @Test
    void shouldReturn500WhenMessageIsNull() throws Exception {
        mockMvc.perform(get("/test/null-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal server error: null"));
    }
}