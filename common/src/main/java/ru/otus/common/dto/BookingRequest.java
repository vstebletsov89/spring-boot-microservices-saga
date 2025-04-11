package ru.otus.common.dto;

import java.math.BigDecimal;

public record BookingRequest(
        String userId,
        String flightNumber,
        BigDecimal price) {}