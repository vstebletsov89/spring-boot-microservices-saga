package ru.otus.flightquery.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
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
import ru.otus.common.event.FlightCreatedEvent;
import ru.otus.flightquery.config.JacksonConfig;
import ru.otus.flightquery.config.KafkaStreamsTestConfig;
import ru.otus.flightquery.publisher.DltPublisher;
import ru.otus.flightquery.service.FlightSyncService;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(classes = {KafkaFlightStreamProcessor.class, JacksonConfig.class, KafkaStreamsTestConfig.class})
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

    @MockitoBean
    private FlightSyncService flightSyncService;

    @MockitoBean
    private DltPublisher dltPublisher;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setupKafkaTemplate() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
    }

    @Test
    void shouldProcessFlightCreatedEvent() throws Exception {

        ZonedDateTime departureTime = ZonedDateTime.parse("2025-04-23T15:07:15.934709900Z");
        ZonedDateTime arrivalTime = departureTime.plusHours(8);

        FlightCreatedEvent event = new FlightCreatedEvent(
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
    void shouldSendToDltOnDeserializationError() {
        String invalidJson = "{ \"type\":\"FlightCreatedEvent\", \"bad\": }";
        kafkaTemplate.send("test-topic", "invalid", invalidJson);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
            verify(dltPublisher).publish(eq("test-dlt"), eq("invalid"), eq(invalidJson));
            verifyNoInteractions(flightSyncService);
        });
    }
}