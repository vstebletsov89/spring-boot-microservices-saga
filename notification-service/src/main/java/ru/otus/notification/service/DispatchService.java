package ru.otus.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.notification.enums.NotificationType;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    public void dispatch(String userId, NotificationType type, String message) {
        switch (type) {
            case EMAIL -> log.info("[EMAIL] Sending to userId={} | message={}", userId, message);
            case SMS   -> log.info("[SMS] Sending to userId={} | message={}", userId, message);
            case PUSH  -> log.info("[PUSH] Sending to userId={} | message={}", userId, message);
            default    -> throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
