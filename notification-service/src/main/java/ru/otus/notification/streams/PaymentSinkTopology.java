package ru.otus.notification.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.notification.dlt.DltPublisher;
import ru.otus.notification.entity.Notification;
import ru.otus.notification.mapper.NotificationMapper;
import ru.otus.notification.repository.ProcessedEventDao;
import ru.otus.notification.service.NotificationWriter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentSinkTopology {

    private final ObjectMapper objectMapper;
    private final NotificationMapper mapper;
    private final NotificationWriter writer;
    private final ProcessedEventDao processedEventDao;
    private final DltPublisher dltPublisher;
    private final Executor writeExecutor;

    @Value("${app.kafka.topic.payments}")
    private String paymentsTopic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> paymentStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(paymentsTopic);
        log.info("Kafka Payment Stream is listening to topic: {}", paymentsTopic);

        stream.foreach((key, value) -> {
            try {
                PaymentEvent event = objectMapper.readValue(value, PaymentEvent.class);

                if (!processedEventDao.tryMarkProcessed(event.eventId())) {
                    return;
                }

                Notification notification = mapper.toNotification(event);
                CompletableFuture
                        .runAsync(() -> writer.save(notification), writeExecutor)
                        .exceptionally(ex -> {
                            log.error("Async save failed for notification id={}", notification.getId(), ex);
                            dltPublisher.publish(dltTopic, key, value);
                            return null;
                        });

            } catch (Exception ex) {
                log.error("Failed to process Kafka message: {}", value, ex);
                dltPublisher.publish(dltTopic, key, value);
            }
        });

        return stream;
    }


}
