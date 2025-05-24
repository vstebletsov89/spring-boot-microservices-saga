package ru.otus.reservation.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
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
import ru.otus.common.saga.BookingCreatedEvent;
import ru.otus.reservation.config.JacksonConfig;
import ru.otus.reservation.config.KafkaStreamsTestConfig;
import ru.otus.reservation.publisher.DltPublisher;
import ru.otus.reservation.service.BookingProcessor;


import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(classes = {KafkaTicketStreamProcessor.class, JacksonConfig.class, KafkaStreamsTestConfig.class})
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

    @MockitoBean
    private BookingProcessor bookingProcessor;

    @MockitoBean
    private DltPublisher dltPublisher;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setupKafkaTemplate() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
    }

    @Test
    void shouldProcessValidBookingEvent() throws Exception {
        var event = new BookingCreatedEvent("1", "FL123", "b1");
        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(new ProducerRecord<>("test-topic", "b1", payload));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(bookingProcessor).process(eq(event));
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
}