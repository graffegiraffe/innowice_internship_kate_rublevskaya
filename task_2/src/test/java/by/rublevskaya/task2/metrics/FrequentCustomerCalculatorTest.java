package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.entities.Customer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class FrequentCustomerCalculatorTest {
    private final List<by.rublevskaya.task2.entities.Order> sampleOrders = by.rublevskaya.task2.TestData.getSampleOrders();

    @Test
    void calculate() {
        FrequentCustomerCalculator frequentCustomerCalculator = new FrequentCustomerCalculator();
        List<Customer> frequentCustomers = frequentCustomerCalculator.calculate(sampleOrders);
        assertEquals(1, frequentCustomers.size(), "wrong number of clients");
        assertEquals("rublevskaya@example.com", frequentCustomers.get(0).getEmail(), "wrong client");
    }
}