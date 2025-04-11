package ru.otus.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ReleaseSeatCommand(
        @TargetAggregateIdentifier String bookingId) {}
