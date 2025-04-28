package ru.otus.flightquery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.kafka.BookingSeatCreatedEvent;
import ru.otus.common.kafka.BookingSeatUpdatedEvent;
import ru.otus.flightquery.repository.BookingSeatMappingRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BookingSyncService.class)
class BookingSyncServiceTest {

    @MockitoBean
    private BookingSeatMappingRepository bookingSeatMappingRepository;

    @Autowired
    private BookingSyncService bookingSyncService;

    private final OffsetDateTime reservedAt = OffsetDateTime.now();

    @Test
    void shouldHandleBookingSeatCreatedEvent() {
        BookingSeatCreatedEvent event = new BookingSeatCreatedEvent(
                "b123",
                "FL123",
                "13C",
                reservedAt,
                BookingStatus.RESERVED
        );

        bookingSyncService.handleCreated(event);

        verify(bookingSeatMappingRepository).save(argThat(b ->
                b.getBookingId().equals(event.bookingId()) &&
                b.getFlightNumber().equals(event.flightNumber()) &&
                b.getSeatNumber().equals(event.seatNumber()) &&
                b.getReservedAt().equals(event.reservedAt()) &&
                b.getStatus() == event.status()
        ));
    }

    @Test
    void shouldHandleBookingSeatUpdatedEvent() {
        BookingSeatMapping existingBooking =
                new BookingSeatMapping(1L, "b123", "FL123",
                        "1A", OffsetDateTime.now(), BookingStatus.RESERVED);

        when(bookingSeatMappingRepository.findByBookingId(anyString()))
                .thenReturn(Optional.of(existingBooking));

         var event = new BookingSeatUpdatedEvent(
                "b123",
                "FL123",
                "14B",
                reservedAt.plusDays(1),
                BookingStatus.PAID
        );

        bookingSyncService.handleUpdated(event);

        verify(bookingSeatMappingRepository).save(argThat(b ->
                b.getBookingId().equals(event.bookingId()) &&
                b.getFlightNumber().equals(event.flightNumber()) &&
                b.getSeatNumber().equals(event.seatNumber()) &&
                b.getReservedAt().equals(event.reservedAt()) &&
                b.getStatus() == event.status()
        ));
    }

    @Test
    void shouldThrowWhenBookingSeatNotFoundForUpdate() {
        when(bookingSeatMappingRepository.findByBookingId("unknown"))
                .thenReturn(Optional.empty());

        BookingSeatUpdatedEvent event = new BookingSeatUpdatedEvent(
                "unknown",
                "SU200",
                "15C",
                reservedAt,
                BookingStatus.CANCELLED
        );

        assertThatThrownBy(() -> bookingSyncService.handleUpdated(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Booking seat mapping not found for bookingId: unknown");
    }
}