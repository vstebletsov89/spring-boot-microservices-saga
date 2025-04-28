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
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.enums.BookingStatus;
import ru.otus.flightquery.service.BookingQueryService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = BookingQueryController.class)
@TestPropertySource(properties = {
        "logging.level.ru.otus.flightquery.controller=DEBUG"
})
@ImportAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        KafkaStreamsDefaultConfiguration.class
})
class BookingQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingQueryService bookingQueryService;

    @MockitoBean
    private StreamsBuilder streamsBuilder;

    @Test
    void shouldReturnBookingsByFlightNumber() throws Exception {
        String flightNumber = "FL123";
        List<BookingSeatMapping> bookings = Collections.singletonList(
                new BookingSeatMapping(1L, "b123", "FL123",
                        "1A", OffsetDateTime.now(), BookingStatus.PAID)
        );

        when(bookingQueryService.findBookingsByFlightNumber(anyString()))
                .thenReturn(bookings);

        mockMvc.perform(get("/api/bookings/flight/{flightNumber}", flightNumber)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookings)));

        verify(bookingQueryService).findBookingsByFlightNumber(flightNumber);
    }

    @Test
    void shouldReturnBookingById() throws Exception {
        String bookingId = "b123";
        BookingSeatMapping booking =
                new BookingSeatMapping(1L, "b123", "FL123",
                "1A", OffsetDateTime.now(), BookingStatus.PAID);

        when(bookingQueryService.findBookingById(anyString()))
                .thenReturn(booking);

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(booking)));

        verify(bookingQueryService).findBookingById(bookingId);
    }
}