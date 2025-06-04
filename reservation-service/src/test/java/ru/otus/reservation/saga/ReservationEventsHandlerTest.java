package ru.otus.reservation.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.saga.*;
import ru.otus.reservation.publisher.DltPublisher;
import ru.otus.reservation.service.BookingSyncService;
import ru.otus.reservation.util.PayloadUtil;

import static org.mockito.Mockito.*;

class ReservationEventsHandlerTest {

    private BookingSyncService bookingSyncService;
    private DltPublisher dltPublisher;
    private PayloadUtil payloadUtil;
    private ReservationEventsHandler handler;

    private final String dltTopic = "dead-letter-topic";

    @BeforeEach
    void setup() {
        bookingSyncService = mock(BookingSyncService.class);
        dltPublisher = mock(DltPublisher.class);
        payloadUtil = mock(PayloadUtil.class);
        handler = new ReservationEventsHandler(bookingSyncService, dltPublisher, payloadUtil);
        injectDltTopic(handler, dltTopic);
    }

    @Test
    void shouldHandleReservationCreatedEventSuccessfully() {
        var event = new BookingCreatedEvent("1", "1", "FL123", "6B");

        handler.on(event);

        verify(bookingSyncService).handleReservationCreated(event);
        verifyNoInteractions(dltPublisher);
    }

    @Test
    void shouldSendToDltOnBookingCreatedEventFailure() {
        var event = new BookingCreatedEvent("b1", "1", "FL123", "6B");
        String payload = "{\"mocked\":true}";
        doThrow(new RuntimeException("Test failure"))
                .when(bookingSyncService).handleReservationCreated(event);
        when(payloadUtil.extractPayload(event)).thenReturn(payload);

        handler.on(event);

        verify(dltPublisher).publish(eq(dltTopic), eq("b1"), eq(payload));
    }

    @Test
    void shouldHandleBookingConfirmedEventSuccessfully() {
        var event = new BookingConfirmedEvent("b1");

        handler.on(event);

        verify(bookingSyncService).handleBookingConfirmed(event);
        verifyNoInteractions(dltPublisher);
    }

    @Test
    void shouldSendToDltOnBookingCancelledEventFailure() {
        var event = new BookingCancelledEvent("b1");
        String payload = "{\"cancel\":true}";
        doThrow(new RuntimeException("Error")).when(bookingSyncService).handleBookingCancelled(event);
        when(payloadUtil.extractPayload(event)).thenReturn(payload);

        handler.on(event);

        verify(dltPublisher).publish(eq(dltTopic), eq("b1"), eq(payload));
    }

    @Test
    void shouldHandlePaymentProcessedEventSuccessfully() {
        var event = new PaymentProcessedEvent("b1", "tx123");

        handler.on(event);

        verify(bookingSyncService).handlePaymentProcessed(event);
        verifyNoInteractions(dltPublisher);
    }

    @Test
    void shouldSendToDltOnBookingCancellationRequestedEventFailure() {
        var event = new BookingCancellationRequestedEvent("b1");
        String payload = "{\"cancelRequest\":true}";
        doThrow(new RuntimeException("Failure"))
                .when(bookingSyncService).handleBookingCancellationRequested(event);
        when(payloadUtil.extractPayload(event)).thenReturn(payload);

        handler.on(event);

        verify(dltPublisher).publish(eq(dltTopic), eq("b1"), eq(payload));
    }

    private void injectDltTopic(ReservationEventsHandler handler, String topic) {
        try {
            var field = ReservationEventsHandler.class.getDeclaredField("dltTopic");
            field.setAccessible(true);
            field.set(handler, topic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dltTopic", e);
        }
    }
}