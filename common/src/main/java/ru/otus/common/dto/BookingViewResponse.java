package ru.otus.common.dto;

public record BookingViewResponse(
        String bookingId,
        String userId,
        String flightNumber,
        String status) {}
