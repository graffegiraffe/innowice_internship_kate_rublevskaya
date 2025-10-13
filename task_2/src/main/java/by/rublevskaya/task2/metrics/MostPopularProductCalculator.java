package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.entities.Order;
import by.rublevskaya.task2.entities.OrderItem;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MostPopularProductCalculator implements MetricCalculator<String> {

    @Override
    public String calculate(List<Order> orders) {
        log.info("Determine the most popular product");
        return orders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName, Collectors.summingInt(OrderItem::getQuantity)))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No products");
    }
}