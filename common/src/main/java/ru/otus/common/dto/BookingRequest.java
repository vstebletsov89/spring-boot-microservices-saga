package ru.otus.common.dto;

public record BookingRequest(
        String userId,
        String flightNumber) {}