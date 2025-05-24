package ru.otus.reservation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.saga.BookingCreatedEvent;
import ru.otus.reservation.config.JacksonConfig;
import ru.otus.reservation.repository.BookingOutboxRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ReservationService.class, JacksonConfig.class})
class ReservationServiceTest {

    @MockitoBean
    private BookingOutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationService ticketService;

    @Test
    void shouldCreateBookingRequestAndSaveToOutbox() {
        BookingCreatedEvent event = new BookingCreatedEvent("1", "FL123", "b1");

        ticketService.createBookingRequest(event);

        verify(outboxRepository).save(argThat(saved ->
        {
            try {
                return saved.getAggregateId().equals("b1") &&
                saved.getPayload().equals(objectMapper.writeValueAsString(event)) &&
                !saved.isSent() &&
                saved.getCreatedAt().isBefore(Instant.now().plusSeconds(1));
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
        ReservationService service = new ReservationService(repository, failingMapper);

        assertThatThrownBy(() -> service.createBookingRequest(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize booking request");

        verifyNoInteractions(repository);
    }
}