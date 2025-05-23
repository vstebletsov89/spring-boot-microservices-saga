package ru.otus.reservation.publisher;

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
import ru.otus.orchestrator.config.KafkaTestConfig;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {DltPublisher.class, KafkaTestConfig.class})
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class DltPublisherTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private DltPublisher dltPublisher;

    private static KafkaConsumer<String, String> consumer;

    @BeforeAll
    static void setUp(@Autowired EmbeddedKafkaBroker broker) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", broker);
        consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singleton("dlt-topic"));
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
    void shouldPublishToKafka() {
        String key = UUID.randomUUID().toString();
        String value = "Test DLT message";

        dltPublisher.publish("dlt-topic", key, value);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "dlt-topic", Duration.ofSeconds(10));
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(key);
        assertThat(record.value()).isEqualTo(value);
    }
}