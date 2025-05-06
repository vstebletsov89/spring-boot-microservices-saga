package ru.otus.flightquery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.response.BookingSeatMappingResponse;
import ru.otus.flightquery.mapper.BookingSeatMappingMapper;
import ru.otus.flightquery.repository.BookingSeatMappingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingQueryService {

    private final BookingSeatMappingRepository bookingSeatMappingRepository;
    private final BookingSeatMappingMapper bookingSeatMappingMapper;

    @Cacheable(value = "bookingsByFlight", key = "#flightNumber")
    @Transactional(readOnly = true)
    public List<BookingSeatMappingResponse> findBookingsByFlightNumber(String flightNumber) {
        log.info("Finding bookings for flight number: {}", flightNumber);

        List<BookingSeatMapping> bookings = bookingSeatMappingRepository.findAllByFlightNumber(flightNumber);
        return bookingSeatMappingMapper.toResponseList(bookings);
    }

    @Cacheable(value = "bookingById", key = "#bookingId")
    @Transactional(readOnly = true)
    public BookingSeatMappingResponse findBookingById(String bookingId) {
        log.info("Finding booking by bookingId: {}", bookingId);

        BookingSeatMapping booking = bookingSeatMappingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking not found for bookingId: {}", bookingId);
                    return new RuntimeException("Booking not found for bookingId: " + bookingId);
                });

        return bookingSeatMappingMapper.toResponse(booking);
    }
}