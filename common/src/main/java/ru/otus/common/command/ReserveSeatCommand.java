package ru.otus.common.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ReserveSeatCommand(
        @TargetAggregateIdentifier String bookingId,
        String flightNumber,
        String userId) {}
