package ru.otus.reservation.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.kafka.ReservationCancelledEvent;
import ru.otus.common.kafka.ReservationCreatedEvent;
import ru.otus.reservation.config.JacksonConfig;
import ru.otus.reservation.repository.BookingOutboxRepository;
import ru.otus.reservation.util.PayloadUtil;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ReservationService.class, JacksonConfig.class})
class ReservationServiceTest {

    @MockitoBean
    private BookingOutboxRepository outboxRepository;

    @MockitoBean
    private PayloadUtil payloadUtil;

    @Autowired
    private ReservationService ticketService;

    @Test
    void shouldCreateBookingRequestAndSaveToOutbox() {
        ReservationCreatedEvent event = new ReservationCreatedEvent(UUID.randomUUID().toString(),"1", "FL123", "b1", "6B");
        String expectedPayload = "{\"mocked\":\"payload\"}";
        when(payloadUtil.extractPayload(event)).thenReturn(expectedPayload);

        ticketService.createBookingRequest(event);

        verify(outboxRepository).save(argThat(saved ->
                saved.getAggregateId().equals("b1") &&
                saved.getPayload().equals(expectedPayload) &&
                !saved.isSent() &&
                saved.getCreatedAt().isBefore(Instant.now().plusSeconds(1))));
    }

    @Test
    void shouldCancelBookingRequestAndSaveToOutbox() {
        ReservationCancelledEvent event = new ReservationCancelledEvent(UUID.randomUUID().toString(), "1", "FL123", "b1");
        String expectedPayload = "{\"mocked\":\"cancelPayload\"}";
        when(payloadUtil.extractPayload(event)).thenReturn(expectedPayload);

        ticketService.cancelBookingRequest(event);

        verify(outboxRepository).save(argThat(saved ->
                saved.getAggregateId().equals("b1") &&
                        saved.getPayload().equals(expectedPayload) &&
                        !saved.isSent() &&
                        saved.getCreatedAt().isBefore(Instant.now().plusSeconds(1))));
    }

    @Test
    void shouldThrowExceptionWhenSerializationFails() throws Exception {
        BookingOutboxRepository repository = mock(BookingOutboxRepository.class);
        ReservationCreatedEvent event = new ReservationCreatedEvent(UUID.randomUUID().toString(),"b1", "FL123", "1", "6B");
        when(payloadUtil.extractPayload(event))
                .thenThrow(new RuntimeException("Failed to serialize booking request"));

        ReservationService service = new ReservationService(repository, payloadUtil);

        assertThatThrownBy(() -> service.createBookingRequest(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize booking request");

        verifyNoInteractions(repository);
    }
}