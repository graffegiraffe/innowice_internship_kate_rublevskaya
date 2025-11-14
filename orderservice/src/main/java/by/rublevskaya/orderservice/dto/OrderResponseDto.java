package by.rublevskaya.orderservice.dto;

import by.rublevskaya.orderservice.model.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private LocalDateTime creationDate;
    private List<OrderItemResponseDto> items;
    private BigDecimal totalPrice;
}
