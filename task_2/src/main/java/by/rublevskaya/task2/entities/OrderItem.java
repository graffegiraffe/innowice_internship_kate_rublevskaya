package by.rublevskaya.task2.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderItem {
    private String productName;
    private int quantity;
    private double price;
    private Category category;
}