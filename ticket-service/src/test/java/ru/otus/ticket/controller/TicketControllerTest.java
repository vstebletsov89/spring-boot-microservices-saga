package ru.otus.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.StreamsBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.common.event.BookingCreatedEvent;
import ru.otus.common.request.BookingRequest;
import ru.otus.ticket.service.TicketService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = TicketController.class)
@ImportAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        KafkaStreamsDefaultConfiguration.class
})
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private StreamsBuilder streamsBuilder;

    @Test
    void shouldAcceptBookingRequest() throws Exception {
        BookingRequest request = new BookingRequest("1", "FL123");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        ArgumentCaptor<BookingCreatedEvent> captor = ArgumentCaptor.forClass(BookingCreatedEvent.class);
        verify(ticketService).createBookingRequest(captor.capture());

        BookingCreatedEvent event = captor.getValue();
        assertThat(event.userId()).isEqualTo("1");
        assertThat(event.flightNumber()).isEqualTo("FL123");
        assertThat(event.bookingId()).isNotBlank();
    }
}