package ru.otus.ticket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.event.BookingCreatedEvent;
import ru.otus.ticket.config.JacksonConfig;
import ru.otus.ticket.repository.BookingOutboxRepository;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TicketService.class, JacksonConfig.class})
class TicketServiceTest {

    @MockitoBean
    private BookingOutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketService ticketService;

    @Test
    void shouldCreateBookingRequestAndSaveToOutbox() {
        BookingCreatedEvent event = new BookingCreatedEvent("1", "FL123", "b1");

        ticketService.createBookingRequest(event);

        verify(outboxRepository).save(argThat(saved ->
        {
            try {
                return saved.getAggregateType().equals("Booking") &&
                saved.getAggregateId().equals("b1") &&
                saved.getPayload().equals(objectMapper.writeValueAsString(event)) &&
                !saved.isSent() &&
                saved.getCreatedAt().isBefore(OffsetDateTime.now().plusSeconds(1));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    void shouldThrowExceptionWhenSerializationFails() throws Exception {
        BookingOutboxRepository repository = mock(BookingOutboxRepository.class);
        ObjectMapper failingMapper = mock(ObjectMapper.class);

        BookingCreatedEvent event = new BookingCreatedEvent("b1", "FL123", "1");
        when(failingMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("FailedParsing") {});
        TicketService service = new TicketService(repository, failingMapper);

        assertThatThrownBy(() -> service.createBookingRequest(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize booking request");

        verifyNoInteractions(repository);
    }
}