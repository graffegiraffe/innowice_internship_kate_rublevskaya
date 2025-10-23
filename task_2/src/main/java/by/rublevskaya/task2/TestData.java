package by.rublevskaya.task2;

import by.rublevskaya.task2.entities.Category;
import by.rublevskaya.task2.entities.Customer;
import by.rublevskaya.task2.entities.Order;
import by.rublevskaya.task2.entities.OrderItem;
import by.rublevskaya.task2.entities.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class TestData {

    public static List<Order> getSampleOrders() {
        Customer customer1 = new Customer("C1", "Kate Rublevskaya", "rublevskaya@example.com", LocalDateTime.now().minusYears(3), 35, "Minsk");
        Customer customer2 = new Customer("C2", "Kate Rub", "rub@example.com", LocalDateTime.now().minusYears(2), 28, "Moscow");
        Customer customer3 = new Customer("C3", "Dima Alikh", "alikh@example.com", LocalDateTime.now().minusYears(5), 45, "Lithuania");
        Customer customer4 = new Customer("C4", "Dzmitry Alikhver", "alikhver@example.com", LocalDateTime.now().minusYears(1), 40, "Poland");

        OrderItem laptop = new OrderItem("computer", 1, 50000.0, Category.ELECTRONICS);
        OrderItem shirt = new OrderItem("skirt", 2, 1000.0, Category.CLOTHING);
        OrderItem book = new OrderItem("star wars", 5, 500.0, Category.BOOKS);
        OrderItem toy = new OrderItem("cat", 1, 700.0, Category.TOYS);
        OrderItem chair = new OrderItem("pillow", 4, 3000.0, Category.HOME);
        OrderItem smartphone = new OrderItem("iphone", 3, 30000.0, Category.ELECTRONICS);

        Order order1 = new Order("O1", LocalDateTime.now().minusDays(10), customer1, List.of(laptop, shirt), OrderStatus.DELIVERED);
        Order order2 = new Order("O2", LocalDateTime.now().minusDays(5), customer2, List.of(book), OrderStatus.DELIVERED);
        Order order3 = new Order("O3", LocalDateTime.now().minusDays(8), customer3, List.of(toy), OrderStatus.DELIVERED);
        Order order4 = new Order("O4", LocalDateTime.now().minusDays(3), customer4, List.of(chair), OrderStatus.DELIVERED);
        Order order5 = new Order("O5", LocalDateTime.now().minusDays(1), customer1, List.of(smartphone), OrderStatus.DELIVERED);
        Order order6 = new Order("O6", LocalDateTime.now().minusDays(2), customer1, List.of(book, chair), OrderStatus.DELIVERED);
        Order order7 = new Order("O7", LocalDateTime.now().minusDays(4), customer1, List.of(toy), OrderStatus.PROCESSING);
        Order order8 = new Order("O8", LocalDateTime.now().minusDays(6), customer2, List.of(shirt), OrderStatus.CANCELLED);
        Order order9 = new Order("O9", LocalDateTime.now().minusDays(7), customer3, List.of(laptop), OrderStatus.SHIPPED);
        Order order10 = new Order("O10", LocalDateTime.now().minusDays(2), customer1, List.of(toy), OrderStatus.DELIVERED);

        return List.of(order1, order2, order3, order4, order5, order6, order7, order8, order9, order10);
    }
}
