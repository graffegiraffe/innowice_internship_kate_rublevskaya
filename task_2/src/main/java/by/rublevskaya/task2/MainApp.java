package by.rublevskaya.task2;

import by.rublevskaya.task2.entities.Order;
import by.rublevskaya.task2.metrics.AverageCheckCalculator;
import by.rublevskaya.task2.metrics.CityMetricCalculator;
import by.rublevskaya.task2.metrics.FrequentCustomerCalculator;
import by.rublevskaya.task2.metrics.MostPopularProductCalculator;
import by.rublevskaya.task2.metrics.RevenueMetricCalculator;
import by.rublevskaya.task2.service.OrderAnalysisService;

import java.util.List;

public class MainApp {
    public static void main(String[] args) {
        List<Order> orders = TestData.getSampleOrders();

        OrderAnalysisService analysisService = new OrderAnalysisService(
                List.of(
                        new CityMetricCalculator(),
                        new RevenueMetricCalculator(),
                        new MostPopularProductCalculator(),
                        new AverageCheckCalculator(),
                        new FrequentCustomerCalculator()
                )
        );

        analysisService.analyzeOrders(orders);
    }
}
