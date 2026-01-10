package by.rublevskaya.orderservice.kafka;

import by.rublevskaya.orderservice.event.OrderEvent;
import by.rublevskaya.orderservice.model.Order;
import by.rublevskaya.orderservice.model.OrderStatus;
import by.rublevskaya.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "create-payment-topic", groupId = "order-group")
    @Transactional
    public void handlePaymentEvent(OrderEvent event) {
        log.info("Received CREATE_PAYMENT event: {}", event);

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("COMPLETED".equals(event.getStatus())) {
            order.setStatus(OrderStatus.SHIPPED); // or COMPLETED
        } else {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getId(), order.getStatus());
    }
}
