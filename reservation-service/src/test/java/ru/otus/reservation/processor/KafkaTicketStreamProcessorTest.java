package ru.otus.reservation.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.kafka.ReservationCancelledEvent;
import ru.otus.common.kafka.ReservationCreatedEvent;
import ru.otus.reservation.cache.EventDeduplicationCache;
import ru.otus.reservation.config.JacksonConfig;
import ru.otus.reservation.config.KafkaStreamsTestConfig;
import ru.otus.reservation.publisher.DltPublisher;
import ru.otus.reservation.service.BookingProcessor;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        KafkaTicketStreamProcessor.class,
        JacksonConfig.class,
        KafkaStreamsTestConfig.class,
        EventDeduplicationCache.class})
@EmbeddedKafka(partitions = 1, topics = {"test-topic", "test-dlt"})
@TestPropertySource(properties = {
        "app.kafka.topic.reservations=test-topic",
        "app.kafka.topic.dlt=test-dlt",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class KafkaTicketStreamProcessorTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventDeduplicationCache eventDeduplicationCache;

    @MockitoBean
    private BookingProcessor bookingProcessor;

    @MockitoBean
    private DltPublisher dltPublisher;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setupKafkaTemplate() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
        eventDeduplicationCache.clear();
    }

    @Test
    void shouldProcessValidBookingEvent() throws Exception {
        var event = new ReservationCreatedEvent(UUID.randomUUID().toString(),"1", "FL123", "b1", "6B");
        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "b1", payload));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(bookingProcessor).sendCreatedCommand(eq(event));
            verifyNoInteractions(dltPublisher);
        });
    }

    @Test
    void shouldSendToDltOnJsonParseError() {
        String invalidJson = """
            {
              "bookingId": "b1",
            }
            """;

        kafkaTemplate.send("test-topic", "b1", invalidJson);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(dltPublisher).publish(eq("test-dlt"), eq("b1"), eq(invalidJson));
            verifyNoInteractions(bookingProcessor);
        });
    }

    @Test
    void shouldSendToDltOnMissingEventId() throws JsonProcessingException {
        ReservationCreatedEvent event = new ReservationCreatedEvent(
                "",
                "1",
                "FL999",
                "b1",
                "6B");
        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send("test-topic", "b1", payload);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(dltPublisher).publish(eq("test-dlt"), eq("b1"), eq(payload));
            verifyNoInteractions(bookingProcessor);
        });
    }

    @Test
    void shouldProcessValidCancellationEvent() throws Exception {
        var event = new ReservationCancelledEvent(UUID.randomUUID().toString(), "b2", "FL123", "1");
        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "b2", payload));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(bookingProcessor).sendCancelledCommand(eq(event));
            verifyNoInteractions(dltPublisher);
        });
    }

    @Test
    void shouldNotProcessDuplicateReservationCreatedEvent() throws Exception {
        String duplicateEventId = UUID.randomUUID().toString();
        ReservationCreatedEvent event = new ReservationCreatedEvent(
                duplicateEventId,
                "1",
                "FL999",
                "b1",
                "6B");

        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL999", payload));
        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL999", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(bookingProcessor, times(1)).sendCreatedCommand(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }

    @Test
    void shouldNotProcessDuplicateReservationCancelledEvent() throws Exception {
        String duplicateEventId = UUID.randomUUID().toString();
        ReservationCancelledEvent event = new ReservationCancelledEvent(
                duplicateEventId,
                "1",
                "FL888",
                "b1");

        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL888", payload));
        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL888", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(bookingProcessor, times(1)).sendCancelledCommand(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }


}