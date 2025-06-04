package ru.otus.reservation.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.saga.*;
import ru.otus.reservation.entity.BookingInfo;
import ru.otus.reservation.repository.BookingRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = BookingSyncService.class)
class BookingSyncServiceTest {

    @Autowired
    private BookingSyncService bookingSyncService;

    @MockitoBean
    private BookingRepository bookingRepository;

    @Test
    void handleReservationCreated_shouldSaveBooking() {
        BookingCreatedEvent event = new BookingCreatedEvent(
                "b1", "1", "FL123","6B"
        );

        bookingSyncService.handleReservationCreated(event);

        verify(bookingRepository, times(1)).save(argThat(saved ->
                saved.getBookingId().equals("b1")
                        && saved.getFlightNumber().equals("FL123")
                        && saved.getSeatNumber().equals("6B")
                        && saved.getStatus() == BookingStatus.RESERVED
                        && saved.getReservedAt() != null
        ));
    }

    @Test
    void handleBookingConfirmed_shouldUpdateStatusToConfirmed_whenMappingExists() {
        BookingInfo mapping = new BookingInfo();
        mapping.setStatus(BookingStatus.RESERVED);
        when(bookingRepository.findByBookingId("b2")).thenReturn(Optional.of(mapping));
        BookingConfirmedEvent event = new BookingConfirmedEvent("b2");

        bookingSyncService.handleBookingConfirmed(event);

        assertThat(mapping.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(mapping);
    }

    @Test
    void handleBookingCancelled_shouldUpdateStatusToCancelled_whenMappingExists() {
        BookingInfo mapping = new BookingInfo();
        mapping.setStatus(BookingStatus.CONFIRMED);
        BookingCancelledEvent event = new BookingCancelledEvent("b4");

        when(bookingRepository.findByBookingId("b4")).thenReturn(Optional.of(mapping));

        bookingSyncService.handleBookingCancelled(event);

        assertThat(mapping.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(mapping);
    }

    @Test
    void handleBookingCancellationRequested_shouldUpdateStatusToCancelled_whenMappingExists() {
        BookingInfo mapping = new BookingInfo();
        mapping.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByBookingId("b6")).thenReturn(Optional.of(mapping));
        BookingCancellationRequestedEvent event = new BookingCancellationRequestedEvent("b6");

        bookingSyncService.handleBookingCancellationRequested(event);

        assertThat(mapping.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(mapping);
    }

    @Test
    void handlePaymentProcessed_shouldUpdateStatusToPaid_whenMappingExists() {
        BookingInfo mapping = new BookingInfo();
        mapping.setStatus(BookingStatus.RESERVED);
        when(bookingRepository.findByBookingId("b8")).thenReturn(Optional.of(mapping));
        PaymentProcessedEvent event = new PaymentProcessedEvent("b8", "1");

        bookingSyncService.handlePaymentProcessed(event);

        assertThat(mapping.getStatus()).isEqualTo(BookingStatus.PAID);
        verify(bookingRepository).save(mapping);
    }

}