package ru.otus.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.common.response.BookingDetailsResponse;
import ru.otus.reservation.mapper.BookingSeatMappingMapper;
import ru.otus.reservation.repository.BookingRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingQueryService {

    private final BookingRepository bookingRepository;

    private final BookingSeatMappingMapper bookingSeatMappingMapper;

    public Optional<BookingDetailsResponse> getByBookingId(String bookingId) {
        return bookingRepository.findByBookingId(bookingId)
                .map(bookingSeatMappingMapper::toResponse);
    }
}
