package ru.otus.flight.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import ru.otus.common.enums.FlightStatus;
import ru.otus.common.kafka.FlightCreatedEvent;
import ru.otus.flight.config.JacksonConfig;
import ru.otus.flight.config.KafkaTestConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {FlightPublisher.class, KafkaTestConfig.class, JacksonConfig.class})
@EmbeddedKafka(partitions = 1, topics = {"flights-topic"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.kafka.topic.flights=flights-topic"
})
class FlightPublisherTest {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private FlightPublisher flightPublisher;

    private static KafkaConsumer<String, String> consumer;

    @BeforeAll
    static void setUp(@Autowired EmbeddedKafkaBroker broker) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", broker);
        consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singleton("flights-topic"));
    }

    @AfterAll
    static void tearDown(@Autowired EmbeddedKafkaBroker broker) {
        consumer.unsubscribe();
        consumer.close(Duration.ofSeconds(5));
        broker.destroy();
    }

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Test
    void shouldPublishFlightCreatedEventToKafka() throws JsonProcessingException {
        String key = UUID.randomUUID().toString();
        FlightCreatedEvent event = getSampleEvent();

        flightPublisher.publish(key, event);

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "flights-topic", Duration.ofSeconds(10));

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(key);
        assertThat(record.value()).isEqualTo(objectMapper.writeValueAsString(event));
    }

    private FlightCreatedEvent getSampleEvent() {
        return new FlightCreatedEvent(
                "FL123",
                "SVO",
                "JFK",
                FlightStatus.SCHEDULED,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(8),
                new BigDecimal("999.99"),
                180,
                0,
                new BigDecimal("10.00")
        );
    }
}