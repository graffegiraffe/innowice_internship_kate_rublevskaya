package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.entities.Customer;
import by.rublevskaya.task2.entities.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FrequentCustomerCalculator implements MetricCalculator<List<Customer>> {

    @Override
    public List<Customer> calculate(List<Order> orders) {
        log.info("We are looking for clients with more than 5 orders");
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getCustomer, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= 5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}