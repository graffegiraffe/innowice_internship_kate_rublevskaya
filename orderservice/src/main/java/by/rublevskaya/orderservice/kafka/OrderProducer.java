package by.rublevskaya.orderservice.kafka;

import by.rublevskaya.orderservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "create-order-topic";

    public void sendOrderEvent(OrderEvent event) {
        log.info("Sending CREATE_ORDER event to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event);
    }
}