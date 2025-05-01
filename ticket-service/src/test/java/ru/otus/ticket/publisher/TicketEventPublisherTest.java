package ru.otus.ticket.publisher;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.ticket.config.KafkaTestConfig;
import ru.otus.ticket.entity.BookingOutboxEvent;
import ru.otus.ticket.repository.BookingOutboxRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TicketEventPublisher.class, KafkaTestConfig.class})
@EmbeddedKafka(partitions = 1, topics = {"outbound-topic"})
@TestPropertySource(properties = {
        "app.kafka.topic.outbound=outbound-topic",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class TicketEventPublisherTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TicketEventPublisher ticketEventPublisher;

    @MockitoBean
    private BookingOutboxRepository bookingOutboxRepository;

    private static KafkaConsumer<String, String> consumer;

    @BeforeAll
    static void setUp(@Autowired EmbeddedKafkaBroker broker) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", broker);
        consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singleton("outbound-topic"));
    }

    @AfterAll
    static void tearDown(@Autowired EmbeddedKafkaBroker broker) {
        consumer.unsubscribe();
        consumer.close(Duration.ofSeconds(5));
        broker.destroy();
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Test
    void shouldPublishAndMarkEventAsSent() {
        String bookingId = UUID.randomUUID().toString();
        String payload = "{\"bookingId\":\"" + bookingId + "\",\"flightNumber\":\"FL123\"}";

        BookingOutboxEvent event = BookingOutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("Booking")
                .aggregateId(bookingId)
                .payload(payload)
                .createdAt(OffsetDateTime.now())
                .sent(false)
                .build();

        when(bookingOutboxRepository.findTop50BySentFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(event));


        ticketEventPublisher.publishPendingEvents();

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "outbound-topic", Duration.ofSeconds(10));
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(bookingId);
        assertThat(record.value()).isEqualTo(payload);

        verify(bookingOutboxRepository).save(event);
    }
}