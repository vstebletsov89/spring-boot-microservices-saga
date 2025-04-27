package ru.otus.flightquery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.event.BookingSeatCreatedEvent;
import ru.otus.common.event.BookingSeatUpdatedEvent;
import ru.otus.flightquery.repository.BookingSeatMappingRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSyncService {

    private final BookingSeatMappingRepository bookingSeatMappingRepository;

    @Transactional
    public void handleCreated(BookingSeatCreatedEvent event) {
        log.info("Syncing new booking seat from event: {}", event);

        BookingSeatMapping bookingSeatMapping = BookingSeatMapping.builder()
                .bookingId(event.bookingId())
                .flightNumber(event.flightNumber())
                .seatNumber(event.seatNumber())
                .reservedAt(event.reservedAt())
                .status(event.status())
                .build();

        bookingSeatMappingRepository.save(bookingSeatMapping);
    }

    @Transactional
    public void handleUpdated(BookingSeatUpdatedEvent event) {
        log.info("Updating booking seat from event: {}", event);

        BookingSeatMapping bookingSeatMapping = bookingSeatMappingRepository
                .findByBookingId(event.bookingId())
                .orElseThrow(() -> new RuntimeException("Booking seat mapping not found for bookingId: " + event.bookingId()));

        bookingSeatMapping.setFlightNumber(event.flightNumber());
        bookingSeatMapping.setSeatNumber(event.seatNumber());
        bookingSeatMapping.setReservedAt(event.reservedAt());
        bookingSeatMapping.setStatus(event.status());

        bookingSeatMappingRepository.save(bookingSeatMapping);
    }
}
