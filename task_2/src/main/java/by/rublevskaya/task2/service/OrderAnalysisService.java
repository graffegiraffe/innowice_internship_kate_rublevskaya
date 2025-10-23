package by.rublevskaya.task2.service;

import by.rublevskaya.task2.entities.Order;
import by.rublevskaya.task2.metrics.MetricCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OrderAnalysisService {
    private final List<MetricCalculator<?>> calculators;

    public void analyzeOrders(List<Order> orders) {
        calculators.forEach(calculator -> {
            log.info("Metrics analysis: {}", calculator.getClass().getSimpleName());
            Object result = calculator.calculate(orders);
            log.info("Result: {}", result);
            System.out.println("Metric: " + calculator.getClass().getSimpleName() + " - Result: " + result);
        });
    }
}
