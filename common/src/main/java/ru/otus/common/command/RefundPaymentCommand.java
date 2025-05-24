package ru.otus.common.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record RefundPaymentCommand(
        @TargetAggregateIdentifier String bookingId) {}
