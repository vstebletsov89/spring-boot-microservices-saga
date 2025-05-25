package ru.otus.common.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CancelFlightCommand(
        @TargetAggregateIdentifier String bookingId,
        String userId,
        String flightNumber) {}