package ru.otus.flightquery.processor;

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
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.kafka.BookingSeatCreatedEvent;
import ru.otus.common.kafka.BookingSeatUpdatedEvent;
import ru.otus.flightquery.config.JacksonConfig;
import ru.otus.flightquery.config.KafkaStreamsTestConfig;
import ru.otus.flightquery.publisher.DltPublisher;
import ru.otus.flightquery.service.BookingSyncService;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(classes = {KafkaBookingStreamProcessor.class, JacksonConfig.class, KafkaStreamsTestConfig.class})
@EmbeddedKafka(partitions = 1, topics = {"test-bookings", "test-dlt"})
@TestPropertySource(properties = {
        "app.kafka.topic.bookings=test-bookings",
        "app.kafka.topic.dlt=test-dlt",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class KafkaBookingStreamProcessorTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingSyncService bookingSyncService;

    @MockitoBean
    private DltPublisher dltPublisher;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setupKafkaTemplate() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
    }

    @Test
    void shouldProcessBookingSeatCreatedEvent() throws Exception {
        OffsetDateTime reservedAt = OffsetDateTime.parse("2025-04-28T12:00:00Z");

        BookingSeatCreatedEvent event = new BookingSeatCreatedEvent(
                "b123",
                "SU100",
                "15A",
                reservedAt,
                BookingStatus.RESERVED
        );


        String payload = objectMapper.createObjectNode()
                .put("type", "BookingSeatCreatedEvent")
                .setAll(objectMapper.convertValue(event, com.fasterxml.jackson.databind.node.ObjectNode.class))
                .toString();

        kafkaTemplate.send(new ProducerRecord<>("test-bookings", "b123", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(bookingSyncService).handleCreated(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }

    @Test
    void shouldProcessBookingSeatUpdatedEvent() throws Exception {
        OffsetDateTime reservedAt = OffsetDateTime.parse("2025-05-01T15:30:00Z");

        BookingSeatUpdatedEvent event = new BookingSeatUpdatedEvent(
                "b456",
                "SU200",
                "17B",
                reservedAt,
                BookingStatus.CANCELLED
        );

        String payload = objectMapper.createObjectNode()
                .put("type", "BookingSeatUpdatedEvent")
                .setAll(objectMapper.convertValue(event, com.fasterxml.jackson.databind.node.ObjectNode.class))
                .toString();

        kafkaTemplate.send(new ProducerRecord<>("test-bookings", "b456", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(bookingSyncService).handleUpdated(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }

    @Test
    void shouldSendToDltOnDeserializationError() {
        String invalidJson = "{ \"type\":\"BookingSeatCreatedEvent\", \"bad\": }";
        kafkaTemplate.send("test-bookings", "invalid", invalidJson);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(dltPublisher).publish(eq("test-dlt"), eq("invalid"), eq(invalidJson));
                    verifyNoInteractions(bookingSyncService);
                });
    }
}