package ru.otus.common.dto;

public record BookingListItem(
        String bookingId,
        String flightNumber,
        String status) {}
