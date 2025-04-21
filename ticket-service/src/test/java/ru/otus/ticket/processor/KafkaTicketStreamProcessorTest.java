package ru.otus.ticket.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.event.BookingCreatedEvent;
import ru.otus.ticket.publisher.DltPublisher;
import ru.otus.ticket.service.BookingProcessor;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = KafkaTicketStreamProcessor.class)
@TestPropertySource(properties = {
        "app.kafka.topic.outbound=test-topic",
        "app.kafka.topic.dlt=test-dlt"
})
class KafkaTicketStreamProcessorTest {

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingProcessor bookingProcessor;

    @MockitoBean
    private DltPublisher dltPublisher;

    @MockitoBean
    private StreamsBuilder builder;

    @MockitoBean
    private KStream<String, String> stream;

    @Autowired
    private KafkaTicketStreamProcessor processor;

    @Test
    void shouldProcessValidBookingEvent() throws Exception {
        String key = "b1";
        String value = """
            {
              "bookingId": "b1",
              "flightNumber": "FL123",
              "userId": "1"
            }
            """;
        BookingCreatedEvent event = new BookingCreatedEvent("1", "FL123", "b1");

        KStream<String, String> mockStream = mock(KStream.class);
        when(builder.<String, String>stream(eq("test-topic"))).thenReturn(mockStream);
        when(objectMapper.readValue(value, BookingCreatedEvent.class)).thenReturn(event);

        ArgumentCaptor<ForeachAction<String, String>> captor = ArgumentCaptor.forClass(ForeachAction.class);
        doAnswer(invocation -> {
            ForeachAction<String, String> action = invocation.getArgument(0);
            action.apply(key, value);
            return null;
        }).when(mockStream).foreach(captor.capture());

        processor.kStream(builder);

        verify(objectMapper).readValue(value, BookingCreatedEvent.class);
        verify(bookingProcessor).process(event);
        verifyNoInteractions(dltPublisher);
    }

    @Test
    void shouldSendToDltOnJsonParseException() throws Exception {
        String key = "b1";
        String value = """
        {
          "bookingId": "invalid",
        }
        """;

        KStream<String, String> mockStream = mock(KStream.class);
        when(builder.<String, String>stream(eq("test-topic"))).thenReturn(mockStream);
        when(objectMapper.readValue(value, BookingCreatedEvent.class))
                .thenThrow(new RuntimeException("Invalid JSON"));

        ArgumentCaptor<ForeachAction<String, String>> captor = ArgumentCaptor.forClass(ForeachAction.class);
        doAnswer(invocation -> {
            ForeachAction<String, String> action = invocation.getArgument(0);
            action.apply(key, value);
            return null;
        }).when(mockStream).foreach(captor.capture());

        processor.kStream(builder);

        verify(objectMapper).readValue(value, BookingCreatedEvent.class);
        verifyNoInteractions(bookingProcessor);
        verify(dltPublisher).publish("test-dlt", key, value);
    }
}