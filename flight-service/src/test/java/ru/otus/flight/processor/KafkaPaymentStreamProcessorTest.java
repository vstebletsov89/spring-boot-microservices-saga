package ru.otus.flight.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.flight.config.JacksonConfig;
import ru.otus.flight.config.KafkaStreamsTestConfig;
import ru.otus.flight.publisher.DltPublisher;
import ru.otus.flight.service.PaymentSyncService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = {
                KafkaPaymentStreamProcessor.class,
                JacksonConfig.class,
                KafkaStreamsTestConfig.class
        }
)
@EmbeddedKafka(partitions = 1, topics = {"test-payments", "test-dlt"})
@TestPropertySource(properties = {
        "app.kafka.topic.payments=test-payments",
        "app.kafka.topic.dlt=test-dlt",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class KafkaPaymentStreamProcessorTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentSyncService paymentSyncService;

    @MockitoBean
    private DltPublisher dltPublisher;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setupKafkaTemplate() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer())
        );
    }

    @Test
    void shouldProcessPaymentEvent() throws Exception {
        PaymentEvent event = new PaymentEvent(
                "e1",
                "b123",
                "john",
                new BigDecimal("199.99"),
                PaymentStatus.SUCCESS,
                null,
                Instant.now()
        );
        String payload = objectMapper.writeValueAsString(event);


        kafkaTemplate.send(new ProducerRecord<>("test-payments", event.bookingId(), payload));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(paymentSyncService).handle(eq(event));
                    verifyNoInteractions(dltPublisher);
                });
    }

    @Test
    void shouldSendToDltOnDeserializationError() {
        String bad = "{ \"bookingId\": }";

        kafkaTemplate.send("test-payments", "key-err", bad);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(dltPublisher).publish(eq("test-dlt"), eq("key-err"), eq(bad));
                    verifyNoInteractions(paymentSyncService);
                });
    }

    @Test
    void shouldSendToDltWhenHandlerThrows() throws Exception {
        PaymentEvent event = new PaymentEvent(
                "e2",
                "b999",
                "user99",
                new BigDecimal("50.00"),
                PaymentStatus.FAILED,
                "card declined",
                Instant.now()
        );
        String payload = objectMapper.writeValueAsString(event);

        doThrow(new RuntimeException("network error"))
                .when(paymentSyncService).handle(eq(event));

        kafkaTemplate.send("test-payments", event.bookingId(), payload);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(paymentSyncService).handle(eq(event));
                    verify(dltPublisher).publish(eq("test-dlt"), eq(event.bookingId()), eq(payload));
                });
    }
}