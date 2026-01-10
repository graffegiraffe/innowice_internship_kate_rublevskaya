package by.rublevskaya.paymentservice.kafka;

import by.rublevskaya.paymentservice.dto.PaymentRequest;
import by.rublevskaya.paymentservice.dto.PaymentResponse;
import by.rublevskaya.paymentservice.event.OrderEvent;
import by.rublevskaya.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final PaymentProducer paymentProducer;

    @KafkaListener(topics = "create-order-topic", groupId = "payment-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received CREATE_ORDER event: {}", event);

        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(event.getOrderId());
            paymentRequest.setUserId(event.getUserId());
            paymentRequest.setAmount(event.getAmount());

            PaymentResponse response = paymentService.createPayment(paymentRequest);
            log.info("Payment processed with status: {}", response.getStatus());

            event.setStatus(response.getStatus().name());

        } catch (Exception e) {
            log.error("Error processing payment for order {}", event.getOrderId(), e);
            event.setStatus("FAILED");
        }

        paymentProducer.sendPaymentEvent(event);
    }
}