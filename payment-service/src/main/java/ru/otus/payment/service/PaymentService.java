package ru.otus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.event.PaymentFailedEvent;
import ru.otus.common.event.PaymentProcessedEvent;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final EventGateway eventGateway;

    public void process(ProcessPaymentCommand cmd) {
        log.info("Processing payment: {}", cmd);

        if (cmd.amount().compareTo(BigDecimal.ZERO) == 0) {
            eventGateway.publish(new PaymentFailedEvent(
                    cmd.bookingId(),
                    cmd.userId(),
                    "Invalid payment amount"
            ));
        } else {
            // call for payment provider
            // dummy payment processed
            eventGateway.publish(new PaymentProcessedEvent(
                    cmd.bookingId(),
                    cmd.userId()
            ));
        }
    }
}
