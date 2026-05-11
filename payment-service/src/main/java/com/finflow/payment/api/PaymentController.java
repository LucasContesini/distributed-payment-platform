package com.finflow.payment.api;

import com.finflow.payment.api.dto.CreatePaymentRequest;
import com.finflow.payment.api.dto.PaymentResponse;
import com.finflow.payment.application.PaymentApplicationService;
import com.finflow.payment.application.command.CreatePaymentCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentApplicationService paymentService;

    public PaymentController(PaymentApplicationService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PaymentResponse createPayment(
            @RequestHeader("X-User-Id") UUID payerId,
            @Valid @RequestBody CreatePaymentRequest request) {

        var command = new CreatePaymentCommand(
                payerId,
                request.payeeId(),
                request.amount(),
                request.currency(),
                request.idempotencyKey()
        );
        return PaymentResponse.from(paymentService.createPayment(command));
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.findById(id));
    }
}
