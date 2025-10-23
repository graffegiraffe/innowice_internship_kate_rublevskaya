package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.TestData;
import by.rublevskaya.task2.entities.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MostPopularProductCalculatorTest {
    private final List<Order> sampleOrders = TestData.getSampleOrders();

    @Test
    void calculate() {
        MostPopularProductCalculator popularProductCalculator = new MostPopularProductCalculator();
        String mostPopularProduct = popularProductCalculator.calculate(sampleOrders);
        assertEquals("star wars", mostPopularProduct, "wrong most popular product");
    }
}