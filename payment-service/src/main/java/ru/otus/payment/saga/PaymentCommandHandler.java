package ru.otus.payment.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Component;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.command.RefundPaymentCommand;
import ru.otus.payment.service.PaymentService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandHandler {

    private final PaymentService paymentService;

    @CommandHandler
    public void handle(ProcessPaymentCommand cmd) {
        log.info("Handling ProcessPaymentCommand: {}", cmd);
        paymentService.process(cmd);
    }

    @CommandHandler
    public void handle(RefundPaymentCommand cmd) {
        log.info("Handling RefundPaymentCommand: {}", cmd);
        paymentService.refund(cmd);
    }
}
