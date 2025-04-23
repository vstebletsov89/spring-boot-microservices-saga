package ru.otus.common.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FlightCreatedEvent.class, name = "FlightCreatedEvent"),
        @JsonSubTypes.Type(value = FlightUpdatedEvent.class, name = "FlightUpdatedEvent")
})
public interface FlightEvent {
}
