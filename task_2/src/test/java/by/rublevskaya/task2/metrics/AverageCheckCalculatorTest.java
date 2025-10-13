package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.TestData;
import by.rublevskaya.task2.entities.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AverageCheckCalculatorTest {
    private final List<Order> sampleOrders = TestData.getSampleOrders();

    @Test
    void calculate() {
        AverageCheckCalculator averageCheckCalculator = new AverageCheckCalculator();
        Double averageCheck = averageCheckCalculator.calculate(sampleOrders);
        assertEquals(24628.571428571428, averageCheck, 0.01, "wrong average check");
    }
}