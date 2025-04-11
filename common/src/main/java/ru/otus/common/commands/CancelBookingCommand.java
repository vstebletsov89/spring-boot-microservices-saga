package ru.otus.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CancelBookingCommand(
        @TargetAggregateIdentifier String bookingId) {}
