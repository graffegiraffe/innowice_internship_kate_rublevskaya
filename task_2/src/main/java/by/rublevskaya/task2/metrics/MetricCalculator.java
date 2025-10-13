package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.entities.Order;

import java.util.List;

public interface MetricCalculator<T> {
    T calculate(List<Order> orders);
}
