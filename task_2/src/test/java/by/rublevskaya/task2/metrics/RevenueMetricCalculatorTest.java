package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.TestData;
import by.rublevskaya.task2.entities.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RevenueMetricCalculatorTest {
    private final List<Order> sampleOrders = TestData.getSampleOrders();

    @Test
    void calculate() {
        RevenueMetricCalculator revenueCalculator = new RevenueMetricCalculator();
        Double totalRevenue = revenueCalculator.calculate(sampleOrders);
        assertEquals(172400.0, totalRevenue, 0.01, "wrong total revenue");
    }
}