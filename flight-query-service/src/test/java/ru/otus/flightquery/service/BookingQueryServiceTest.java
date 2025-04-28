package ru.otus.flightquery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.enums.BookingStatus;
import ru.otus.flightquery.repository.BookingSeatMappingRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BookingQueryService.class)
class BookingQueryServiceTest {

    @MockitoBean
    private BookingSeatMappingRepository bookingSeatMappingRepository;

    @Autowired
    private BookingQueryService bookingQueryService;

    @Test
    void shouldReturnBookingsByFlightNumber() {
        String flightNumber = "FL123";
        BookingSeatMapping booking =
                new BookingSeatMapping(1L, "b123", "FL123",
                        "1A", OffsetDateTime.now(), BookingStatus.PAID);

        when(bookingSeatMappingRepository.findAllByFlightNumber(flightNumber))
                .thenReturn(List.of(booking));

        List<BookingSeatMapping> result = bookingQueryService.findBookingsByFlightNumber(flightNumber);

        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .contains(booking);
    }

    @Test
    void shouldReturnEmptyListWhenNoBookingsFoundByFlightNumber() {
        String flightNumber = "FL123";

        when(bookingSeatMappingRepository.findAllByFlightNumber(flightNumber))
                .thenReturn(List.of());

        List<BookingSeatMapping> result = bookingQueryService.findBookingsByFlightNumber(flightNumber);

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldReturnBookingById() {
        String bookingId = "b123";
        BookingSeatMapping booking =
                new BookingSeatMapping(1L, "b123", "FL123",
                        "1A", OffsetDateTime.now(), BookingStatus.PAID);

        when(bookingSeatMappingRepository.findByBookingId(bookingId))
                .thenReturn(Optional.of(booking));

        BookingSeatMapping result = bookingQueryService.findBookingById(bookingId);

        assertThat(result)
                .isNotNull()
                .isEqualTo(booking);
    }

    @Test
    void shouldThrowExceptionWhenBookingNotFoundById() {
        String bookingId = "invalid";

        when(bookingSeatMappingRepository.findByBookingId(bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingQueryService.findBookingById(bookingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Booking not found for bookingId: " + bookingId);
    }
}