package ru.otus.common.kafka;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReservationCreatedEvent.class, name = "ReservationCreatedEvent"),
        @JsonSubTypes.Type(value = ReservationCancelledEvent.class, name = "ReservationCancelledEvent")
})
public interface ReservationEvent {
}
