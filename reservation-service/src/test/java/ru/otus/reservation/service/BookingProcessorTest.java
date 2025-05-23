package ru.otus.reservation.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.BookFlightCommand;
import ru.otus.common.saga.BookingCreatedEvent;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BookingProcessor.class)
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {
        "logging.level.ru.otus.orchestrator.service.BookingProcessor=DEBUG"
})
class BookingProcessorTest {

    @MockitoBean
    private CommandGateway commandGateway;

    @Autowired
    private BookingProcessor bookingProcessor;

    @Test
    void shouldSendBookFlightCommand() {
        BookingCreatedEvent event = new BookingCreatedEvent("1", "FL123", "b1");

        CompletableFuture<Object> future = CompletableFuture.completedFuture("success");
        when(commandGateway.send(any())).thenReturn(future);

        bookingProcessor.process(event);

        var captor = ArgumentCaptor.forClass(BookFlightCommand.class);
        verify(commandGateway).send(captor.capture());

        BookFlightCommand sentCommand = captor.getValue();
        assertThat(sentCommand.bookingId()).isEqualTo("b1");
        assertThat(sentCommand.userId()).isEqualTo("1");
        assertThat(sentCommand.flightNumber()).isEqualTo("FL123");
    }

    @Test
    void shouldLogErrorIfCommandFails(CapturedOutput output) {
        BookingCreatedEvent event = new BookingCreatedEvent("2", "FL456", "b2");

        CompletableFuture<Object> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Command failed"));
        when(commandGateway.send(any())).thenReturn(failedFuture);

        bookingProcessor.process(event);

        verify(commandGateway).send(any(BookFlightCommand.class));
        assertThat(output.getAll())
                .contains("Failed to send command: BookFlightCommand[bookingId=b2, userId=2, flightNumber=FL456]");
    }
}