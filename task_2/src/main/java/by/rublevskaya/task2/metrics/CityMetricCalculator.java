package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.entities.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class CityMetricCalculator implements MetricCalculator<Set<String>> {

    @Override
    public Set<String> calculate(List<Order> orders) {
        log.info("Counting unique cities");
        return orders.stream()
                .map(order -> order.getCustomer().getCity())
                .collect(Collectors.toSet());
    }
}