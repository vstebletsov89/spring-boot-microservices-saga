package ru.otus.flightquery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.StreamsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.RoundTripFlightResponse;
import ru.otus.flightquery.service.FlightQueryService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightQueryController.class)
@TestPropertySource(properties = {
        "logging.level.ru.otus.flight.controller=DEBUG"
})
@ImportAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        KafkaStreamsDefaultConfiguration.class
})
class FlightQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlightQueryService flightQueryService;

    @MockitoBean
    private StreamsBuilder streamsBuilder;

    @Test
    void shouldReturnRoundTripFlights() throws Exception {
        FlightSearchRequest request = new FlightSearchRequest(
                "SVO", "CDG",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7),
                2
        );

        RoundTripFlightResponse response = new RoundTripFlightResponse(
                Collections.emptyList(), Collections.emptyList()
        );

        when(flightQueryService.searchRoundTripFlights(any(FlightSearchRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/flights/search")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(flightQueryService).searchRoundTripFlights(any(FlightSearchRequest.class));
    }
}