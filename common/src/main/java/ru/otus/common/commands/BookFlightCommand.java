package ru.otus.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

public record BookFlightCommand(
        @TargetAggregateIdentifier String bookingId,
        String userId,
        String flightNumber,
        BigDecimal price) {}