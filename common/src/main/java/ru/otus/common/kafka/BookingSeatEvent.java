package ru.otus.common.kafka;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BookingSeatCreatedEvent.class, name = "BookingSeatCreatedEvent"),
        @JsonSubTypes.Type(value = BookingSeatUpdatedEvent.class, name = "BookingSeatUpdatedEvent")
})
public interface BookingSeatEvent {
}
