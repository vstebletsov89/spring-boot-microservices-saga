package ru.otus.common.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ReleaseSeatCommand(
        @TargetAggregateIdentifier String bookingId) {}
