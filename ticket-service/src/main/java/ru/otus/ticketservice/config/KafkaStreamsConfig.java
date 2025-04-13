package ru.otus.ticketservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;
import ru.otus.common.dto.BookingRequest;

import java.util.Properties;

@Configuration
public class KafkaStreamsConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaStreamsConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public StreamsConfig streamsConfig() {
        Properties props = new Properties();
        props.putAll(kafkaProperties.buildStreamsProperties());
        return new StreamsConfig(props);
    }

    @Bean
    public KStream<String, BookingRequest> bookingStream(StreamsBuilder builder) {
        JsonSerde<BookingRequest> serde = new JsonSerde<>(BookingRequest.class, new ObjectMapper());

        KStream<String, BookingRequest> stream = builder
                .stream(kafkaProperties.getStreams().getApplicationId(),
                        Consumed.with(Serdes.String(), serde));

        stream.peek((key, value) -> System.out.println("Stream received: " + value))
                .to("ticket.outbound");

        return stream;
    }
}
