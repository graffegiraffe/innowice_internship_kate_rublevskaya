package by.rublevskaya.paymentservice.dto;

import by.rublevskaya.paymentservice.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private BigDecimal paymentAmount;
    private PaymentStatus status;
    private LocalDateTime timestamp;
}