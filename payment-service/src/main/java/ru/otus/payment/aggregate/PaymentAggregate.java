package ru.otus.payment.aggregate;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.event.PaymentFailedEvent;
import ru.otus.common.event.PaymentProcessedEvent;
import ru.otus.payment.service.PaymentService;

@Aggregate
@Slf4j
public class PaymentAggregate {

    @AggregateIdentifier
    private String bookingId;

    public PaymentAggregate() {}

    @CommandHandler
    public PaymentAggregate(ProcessPaymentCommand cmd, PaymentService paymentService) {
        paymentService.process(cmd);
    }

    @EventSourcingHandler
    public void onProcessed(PaymentProcessedEvent event) {
        this.bookingId = event.bookingId();
    }

    @EventSourcingHandler
    public void onFailed(PaymentFailedEvent event) {
        this.bookingId = event.bookingId();
    }
}
