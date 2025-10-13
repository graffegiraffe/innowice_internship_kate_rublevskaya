package by.rublevskaya.task2.metrics;

import by.rublevskaya.task2.entities.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CityMetricCalculatorTest {
    private final List<Order> sampleOrders = by.rublevskaya.task2.TestData.getSampleOrders();

    @Test
    void calculate() {
        CityMetricCalculator cityMetricCalculator = new CityMetricCalculator();
        Set<String> uniqueCities = cityMetricCalculator.calculate(sampleOrders);
        assertEquals(Set.of("Minsk", "Moscow", "Lithuania", "Poland"),
                uniqueCities, "wrong cities");
    }
}