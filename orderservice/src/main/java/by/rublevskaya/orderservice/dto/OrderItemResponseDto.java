package by.rublevskaya.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponseDto {
    private Long itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
}
