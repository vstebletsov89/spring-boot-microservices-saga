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
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.kafka.BookingSeatCreatedEvent;
import ru.otus.flight.config.JacksonConfig;
import ru.otus.flight.config.KafkaTestConfig;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BookingPublisher.class, KafkaTestConfig.class, JacksonConfig.class})
@EmbeddedKafka(partitions = 1, topics = {"bookings-topic"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.kafka.topic.bookings=bookings-topic"
})
class BookingPublisherTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private BookingPublisher bookingPublisher;

    private static KafkaConsumer<String, String> consumer;

    @BeforeAll
    static void setUp(@Autowired EmbeddedKafkaBroker broker) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", broker);
        consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singleton("bookings-topic"));
    }

    @AfterAll
    static void tearDown() {
        consumer.close();
    }

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Test
    void shouldPublishBookingEventToKafka() throws JsonProcessingException {
        String key = UUID.randomUUID().toString();
        BookingSeatCreatedEvent event = getSampleEvent();

        bookingPublisher.publish(key, event);

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "bookings-topic", Duration.ofSeconds(10));

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(key);
        assertThat(record.value()).isEqualTo(objectMapper.writeValueAsString(event));
    }

    private BookingSeatCreatedEvent getSampleEvent() {
        return new BookingSeatCreatedEvent(
                  "B123",
                "FL123",
                "13A",
                OffsetDateTime.now(),
                BookingStatus.RESERVED
        );
    }
}