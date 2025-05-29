package ru.otus.reservation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayloadUtil {

    private final ObjectMapper objectMapper;

    public String extractPayload(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("Failed to serialize event: {}", event, e);
            throw new RuntimeException("Failed to serialize booking request", e);
        }
    }
}
