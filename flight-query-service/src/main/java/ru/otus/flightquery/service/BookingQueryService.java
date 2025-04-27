package ru.otus.flightquery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.flightquery.repository.BookingSeatMappingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingQueryService {

    private final BookingSeatMappingRepository bookingSeatMappingRepository;

    @Transactional(readOnly = true)
    public List<BookingSeatMapping> findBookingsByFlightNumber(String flightNumber) {
        log.info("Finding bookings for flight number: {}", flightNumber);
        return bookingSeatMappingRepository.findAllByFlightNumber(flightNumber);
    }

    @Transactional(readOnly = true)
    public BookingSeatMapping findBookingById(String bookingId) {
        log.info("Finding booking by bookingId: {}", bookingId);
        return bookingSeatMappingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for bookingId: " + bookingId));
    }
}