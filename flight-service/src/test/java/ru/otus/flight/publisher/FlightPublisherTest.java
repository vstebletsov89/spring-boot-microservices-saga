package ru.otus.flight.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.otus.common.enums.FlightStatus;
import ru.otus.common.event.FlightCreatedEvent;
import utils.TestUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FlightPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private FlightPublisher flightPublisher;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        MockitoAnnotations.openMocks(this);
        TestUtils.setField(flightPublisher, "topic", "flights-topic");
    }

    @Test
    void shouldPublishEventSuccessfully() throws Exception {
        FlightCreatedEvent event = getSampleEvent();
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(
                new SendResult<>(null, null));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(future);

        flightPublisher.publish("key-123", event);

        verify(kafkaTemplate, times(1))
                .send(eq("flights-topic"), eq("key-123"), anyString());
    }

    @Test
    void shouldLogSerializationError() throws Exception {
        Object badObject = mock(Object.class);
        when(objectMapper.writeValueAsString(badObject)).thenThrow(JsonProcessingException.class);

        flightPublisher.publish("key-bad", badObject);

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    private FlightCreatedEvent getSampleEvent() {
        return new FlightCreatedEvent(
                "FL123",
                "SVO",
                "JFK",
                FlightStatus.SCHEDULED,
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(1).plusHours(8),
                new BigDecimal("999.99"),
                180,
                0,
                new BigDecimal("10.00")
        );
    }
}