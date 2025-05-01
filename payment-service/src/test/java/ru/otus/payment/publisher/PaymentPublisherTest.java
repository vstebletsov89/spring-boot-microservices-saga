package ru.otus.payment.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.payment.config.KafkaTestConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = { PaymentPublisher.class, KafkaTestConfig.class }
)
@EmbeddedKafka(partitions = 1, topics = { "payments-topic" })
@TestPropertySource(properties = {
        "app.kafka.topic.payments=payments-topic",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@ExtendWith(OutputCaptureExtension.class)
class PaymentPublisherTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private PaymentPublisher paymentPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    private static KafkaConsumer<String, String> consumer;

    @BeforeAll
    static void setUp(@Autowired EmbeddedKafkaBroker broker) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                "testGroup", "true", broker
        );
        consumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singleton("payments-topic"));
    }

    @AfterAll
    static void tearDown(@Autowired EmbeddedKafkaBroker broker) {
        consumer.unsubscribe();
        consumer.close(Duration.ofSeconds(5));
        broker.destroy();
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers", "localhost:9092"));
    }

    @Test
    void shouldSerializeAndSendPaymentEvent() throws Exception {
        String key = UUID.randomUUID().toString();
        PaymentEvent event = new PaymentEvent(
                key,
                "booking-123",
                "user-456",
                new BigDecimal("250.75"),
                PaymentStatus.SUCCESS,
                null,
                Instant.now()
        );

        paymentPublisher.publish(key, event);

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "payments-topic", Duration.ofSeconds(10));
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(key);

        String expectedJson = objectMapper.writeValueAsString(event);
        assertThat(record.value()).isEqualTo(expectedJson);
    }

    @Test
    void shouldLogErrorIfCommandFails(CapturedOutput output) throws Exception {
        Object event = new Object();
        ObjectMapper failedMapper = Mockito.mock(ObjectMapper.class);
        when(failedMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("Error") {});

        var kafkaTemplate = mock(KafkaTemplate.class);
        String topic = "payments-topic";
        var paymentPublisherFailed = new PaymentPublisher(kafkaTemplate, failedMapper);
        paymentPublisherFailed.publish("test", event);

        assertThat(output.getOut())
                .contains("Failed to serialize event")
                .contains(event.toString());
        verifyNoInteractions(kafkaTemplate);
    }
}