package ru.otus.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ConfirmBookingCommand(
        @TargetAggregateIdentifier String bookingId) {}