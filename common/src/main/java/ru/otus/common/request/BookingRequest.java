package ru.otus.common.request;

public record BookingRequest(
        String userId,
        String flightNumber) {}