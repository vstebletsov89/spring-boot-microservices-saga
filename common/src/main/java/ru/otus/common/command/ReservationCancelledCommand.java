package ru.otus.common.command;

public record ReservationCancelledCommand(
        String bookingId) {}
