package ru.otus.flight.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.BookingSeatUpdatedEvent;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.flight.publisher.BookingPublisher;
import ru.otus.flight.repository.BookingSeatMappingRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = PaymentSyncService.class)
class PaymentSyncServiceTest {

    @MockitoBean
    private BookingSeatMappingRepository mappingRepository;

    @MockitoBean
    private BookingPublisher bookingPublisher;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private PaymentSyncService paymentSyncService;

    private BookingSeatMapping mapping;

    @BeforeEach
    void setUp() {
        mapping = new BookingSeatMapping();
        mapping.setId(42L);
        mapping.setBookingId("b42");
        mapping.setFlightNumber("FL42");
        mapping.setSeatNumber("6A");
        mapping.setReservedAt(Instant.now());
        mapping.setStatus(BookingStatus.RESERVED);
    }

    @Test
    void whenPaymentSuccess_thenStatusPaidAndEventPublished() {
        Instant now = Instant.now();
        PaymentEvent event = new PaymentEvent(
                "e1",
                mapping.getBookingId(),
                "u1",
                new BigDecimal("123.45"),
                PaymentStatus.SUCCESS,
                null,
                now
        );
        when(mappingRepository.findByBookingId(mapping.getBookingId()))
                .thenReturn(Optional.of(mapping));

        paymentSyncService.handle(event);

        assertThat(mapping.getStatus()).isEqualTo(BookingStatus.PAID);
        assertThat(mapping.getReservedAt()).isEqualTo(now);
        verify(mappingRepository).save(mapping);


        verify(bookingPublisher).publish(
                eq(mapping.getBookingId()),
                eq(new BookingSeatUpdatedEvent(
                        mapping.getBookingId(),
                        mapping.getFlightNumber(),
                        mapping.getSeatNumber(),
                        now,
                        BookingStatus.PAID
                ))
        );
    }

    @Test
    void whenPaymentFailure_thenStatusCancelledAndEventPublished() {
        Instant now = Instant.now();
        PaymentEvent event = new PaymentEvent(
                "e2",
                mapping.getBookingId(),
                "u2",
                new BigDecimal("50.00"),
                PaymentStatus.FAILED,
                "card declined",
                now
        );
        when(mappingRepository.findByBookingId(mapping.getBookingId()))
                .thenReturn(Optional.of(mapping));

        paymentSyncService.handle(event);

        assertThat(mapping.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(mapping.getReservedAt()).isEqualTo(now);
        verify(mappingRepository).save(mapping);

        verify(bookingPublisher).publish(
                eq(mapping.getBookingId()),
                eq(new BookingSeatUpdatedEvent(
                        mapping.getBookingId(),
                        mapping.getFlightNumber(),
                        mapping.getSeatNumber(),
                        now,
                        BookingStatus.CANCELLED
                ))
        );
    }

    @Test
    void whenMappingNotFound_thenThrowsRuntimeException() {
        PaymentEvent event = new PaymentEvent(
                "invalid",
                "unknown-booking",
                "u3",
                BigDecimal.ZERO,
                PaymentStatus.SUCCESS,
                null,
                Instant.now()
        );
        when(mappingRepository.findByBookingId(event.bookingId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentSyncService.handle(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("BookingSeatMapping not found for bookingId=" + event.bookingId());

        verify(mappingRepository).findByBookingId(event.bookingId());
        verifyNoMoreInteractions(mappingRepository, bookingPublisher);
    }
}