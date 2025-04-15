package ru.otus.common.response;

public record BookingViewResponse(
        String bookingId,
        String userId,
        String flightNumber,
        String status) {}
