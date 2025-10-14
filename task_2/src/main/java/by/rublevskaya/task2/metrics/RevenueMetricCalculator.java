package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.entities.Order;
import by.rublevskaya.task2.entities.OrderStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RevenueMetricCalculator implements MetricCalculator<Double> {

    @Override
    public Double calculate(List<Order> orders) {
        log.info("Calculation of total income from completed orders");
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
    }
}