package by.rublevskaya.paymentservice.controller;

import by.rublevskaya.paymentservice.dto.PaymentRequest;
import by.rublevskaya.paymentservice.dto.PaymentResponse;
import by.rublevskaya.paymentservice.model.PaymentStatus;
import by.rublevskaya.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PaymentResponse>> getByStatuses(@RequestParam List<PaymentStatus> status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatuses(status));
    }

    @GetMapping("/summary")
    public ResponseEntity<BigDecimal> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(paymentService.getTotalSum(from, to));
    }
}