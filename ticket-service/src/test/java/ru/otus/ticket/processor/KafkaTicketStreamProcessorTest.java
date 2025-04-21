package ru.otus.ticket.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
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
import ru.otus.common.event.BookingCreatedEvent;
import ru.otus.ticket.publisher.DltPublisher;
import ru.otus.ticket.service.BookingProcessor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-topic", "test-dlt"})
@TestPropertySource(properties = {
        "app.kafka.topic.outbound=test-topic",
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

    private KafkaTemplate<String, String> createKafkaTemplate() {
        Map<String, Object> configs = KafkaTestUtils.producerProps(embeddedKafka);
        var producerFactory = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer());
        return new KafkaTemplate<>(producerFactory);
    }

    @Test
    void shouldProcessValidBookingEvent() throws Exception {
        var event = new BookingCreatedEvent("1", "FL123", "b1");
        String payload = objectMapper.writeValueAsString(event);

        KafkaTemplate<String, String> kafkaTemplate = createKafkaTemplate();
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

        KafkaTemplate<String, String> kafkaTemplate = createKafkaTemplate();
        kafkaTemplate.send("test-topic", "b1", invalidJson);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(dltPublisher).publish(eq("test-dlt"), eq("b1"), eq(invalidJson));
            verifyNoInteractions(bookingProcessor);
        });
    }
}