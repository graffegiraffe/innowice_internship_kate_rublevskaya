package by.rublevskaya.paymentservice.kafka;

import by.rublevskaya.paymentservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "create-payment-topic";

    public void sendPaymentEvent(OrderEvent event) {
        log.info("Sending CREATE_PAYMENT event result to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event);
    }
}
