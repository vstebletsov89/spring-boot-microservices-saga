package ru.otus.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ReserveSeatCommand(
        @TargetAggregateIdentifier String bookingId,
        String flightNumber,
        String userId) {}
