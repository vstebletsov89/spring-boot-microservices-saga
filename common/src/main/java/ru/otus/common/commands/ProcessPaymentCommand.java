package ru.otus.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ProcessPaymentCommand(
        @TargetAggregateIdentifier String bookingId,
        String userId,
        double amount) {}