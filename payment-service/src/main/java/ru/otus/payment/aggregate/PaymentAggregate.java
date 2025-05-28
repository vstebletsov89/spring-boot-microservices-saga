package ru.otus.payment.aggregate;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.command.RefundPaymentCommand;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;
import ru.otus.common.saga.PaymentRefundedEvent;
import ru.otus.payment.service.PaymentService;

import static org.axonframework.modelling.command.AggregateCreationPolicy.ALWAYS;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

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

    @CreationPolicy(ALWAYS)
    @CommandHandler
    public void handle(RefundPaymentCommand cmd, PaymentService paymentService) {
        paymentService.refund(cmd);
        apply(new PaymentRefundedEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void onProcessed(PaymentProcessedEvent event) {
        this.bookingId = event.bookingId();
    }

    @EventSourcingHandler
    public void onFailed(PaymentFailedEvent event) {
        this.bookingId = event.bookingId();
    }

    @EventSourcingHandler
    public void on(PaymentRefundedEvent event) {
        this.bookingId = event.bookingId();
        log.info("Refunded payment for bookingId: {}", event.bookingId());
    }
}
