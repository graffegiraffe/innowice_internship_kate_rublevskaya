package by.rublevskaya.paymentservice.dto;

import by.rublevskaya.paymentservice.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal paymentAmount;
    private PaymentStatus status;
    private LocalDateTime timestamp;
}