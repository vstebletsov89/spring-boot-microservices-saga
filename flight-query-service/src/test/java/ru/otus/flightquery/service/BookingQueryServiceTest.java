package ru.otus.flightquery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.response.BookingSeatMappingResponse;
import ru.otus.flightquery.mapper.BookingSeatMappingMapper;
import ru.otus.flightquery.mapper.BookingSeatMappingMapperImpl;
import ru.otus.flightquery.repository.BookingSeatMappingRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {BookingQueryService.class, BookingSeatMappingMapperImpl.class})
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

        List<BookingSeatMappingResponse> result = bookingQueryService.findBookingsByFlightNumber(flightNumber);

        assertThat(result)
                .isNotEmpty()
                .hasSize(1);

        BookingSeatMappingResponse response = result.get(0);
        assertThat(response.bookingId()).isEqualTo(booking.getBookingId());
        assertThat(response.flightNumber()).isEqualTo(booking.getFlightNumber());
        assertThat(response.seatNumber()).isEqualTo(booking.getSeatNumber());
        assertThat(response.status()).isEqualTo(booking.getStatus());
    }

    @Test
    void shouldReturnEmptyListWhenNoBookingsFoundByFlightNumber() {
        String flightNumber = "FL123";

        when(bookingSeatMappingRepository.findAllByFlightNumber(flightNumber))
                .thenReturn(List.of());

        List<BookingSeatMappingResponse> result = bookingQueryService.findBookingsByFlightNumber(flightNumber);

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

        BookingSeatMappingResponse result = bookingQueryService.findBookingById(bookingId);

        assertThat(result).isNotNull();
        assertThat(result.bookingId()).isEqualTo(booking.getBookingId());
        assertThat(result.flightNumber()).isEqualTo(booking.getFlightNumber());
        assertThat(result.seatNumber()).isEqualTo(booking.getSeatNumber());
        assertThat(result.status()).isEqualTo(booking.getStatus());
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