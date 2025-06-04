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
import ru.otus.common.enums.FlightStatus;
import ru.otus.common.kafka.FlightCreatedEvent;
import ru.otus.common.kafka.FlightUpdatedEvent;
import ru.otus.flightquery.cache.EventDeduplicationCache;
import ru.otus.flightquery.config.JacksonConfig;
import ru.otus.flightquery.config.KafkaStreamsTestConfig;
import ru.otus.flightquery.publisher.DltPublisher;
import ru.otus.flightquery.service.FlightSyncService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        KafkaFlightStreamProcessor.class,
        JacksonConfig.class,
        KafkaStreamsTestConfig.class,
        EventDeduplicationCache.class})
@EmbeddedKafka(partitions = 1, topics = {"test-topic", "test-dlt"})
@TestPropertySource(properties = {
        "app.kafka.topic.flights=test-topic",
        "app.kafka.topic.dlt=test-dlt",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class KafkaFlightStreamProcessorTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventDeduplicationCache eventDeduplicationCache;

    @MockitoBean
    private FlightSyncService flightSyncService;

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
    void shouldProcessFlightCreatedEvent() throws Exception {

        LocalDateTime departureTime = LocalDateTime.parse("2025-04-23T15:07:15");
        LocalDateTime arrivalTime = departureTime.plusHours(8);

        FlightCreatedEvent event = new FlightCreatedEvent(
                UUID.randomUUID().toString(),
                "FL123", "SVO", "JFK",
                ru.otus.common.enums.FlightStatus.SCHEDULED,
                departureTime,
                arrivalTime,
                new BigDecimal("999.99"),
                180, 0, new BigDecimal("10.00"));

        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL123", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
            verify(flightSyncService).handleCreated(eq(event));
            verifyNoInteractions(dltPublisher);
        });
    }

    @Test
    void shouldProcessFlightUpdatedEvent() throws Exception {

        LocalDateTime departureTime = LocalDateTime.parse("2025-04-25T12:00:00");
        LocalDateTime arrivalTime = departureTime.plusHours(6);

        FlightUpdatedEvent event = new FlightUpdatedEvent(
                UUID.randomUUID().toString(),
                "FL456",
                FlightStatus.DELAYED,
                departureTime,
                arrivalTime,
                new BigDecimal("799.99"),
                200,
                50,
                new BigDecimal("5.00")
        );

        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL456", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(flightSyncService).handleUpdated(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }

    @Test
    void shouldSendToDltOnDeserializationError() {
        String invalidJson = "{ \"type\":\"FlightCreatedEvent\", \"bad\": }";
        kafkaTemplate.send("test-topic", "invalid", invalidJson);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
            verify(dltPublisher).publish(eq("test-dlt"), eq("invalid"), eq(invalidJson));
            verifyNoInteractions(flightSyncService);
        });
    }

    @Test
    void shouldNotProcessDuplicateFlightCreatedEvent() throws Exception {
        LocalDateTime departureTime = LocalDateTime.parse("2025-04-23T15:07:15");
        LocalDateTime arrivalTime = departureTime.plusHours(8);
        String duplicateEventId = UUID.randomUUID().toString();

        FlightCreatedEvent event = new FlightCreatedEvent(
                duplicateEventId,
                "FL999", "SVO", "JFK",
                FlightStatus.SCHEDULED,
                departureTime,
                arrivalTime,
                new BigDecimal("199.99"),
                180, 0, new BigDecimal("10.00"));

        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL999", payload));
        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL999", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(flightSyncService, times(1)).handleCreated(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }

    @Test
    void shouldNotProcessDuplicateFlightUpdatedEvent() throws Exception {
        LocalDateTime departureTime = LocalDateTime.parse("2025-04-25T12:00:00");
        LocalDateTime arrivalTime = departureTime.plusHours(6);
        String duplicateEventId = UUID.randomUUID().toString();

        FlightUpdatedEvent event = new FlightUpdatedEvent(
                duplicateEventId,
                "FL888",
                FlightStatus.DELAYED,
                departureTime,
                arrivalTime,
                new BigDecimal("888.88"),
                200,
                50,
                new BigDecimal("5.00")
        );

        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL888", payload));
        kafkaTemplate.send(new ProducerRecord<>("test-topic", "FL888", payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(flightSyncService, times(1)).handleUpdated(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }
}