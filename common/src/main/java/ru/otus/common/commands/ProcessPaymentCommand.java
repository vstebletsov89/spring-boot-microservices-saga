package ru.otus.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

public record ProcessPaymentCommand(
        @TargetAggregateIdentifier String bookingId,
        String userId,
        BigDecimal amount) {}