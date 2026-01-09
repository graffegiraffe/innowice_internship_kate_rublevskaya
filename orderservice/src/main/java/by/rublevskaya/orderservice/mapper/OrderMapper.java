package by.rublevskaya.orderservice.mapper;

import by.rublevskaya.orderservice.dto.OrderItemResponseDto;
import by.rublevskaya.orderservice.dto.OrderResponseDto;
import by.rublevskaya.orderservice.model.Order;
import by.rublevskaya.orderservice.model.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "user", ignore = true)
    OrderResponseDto toResponseDto(Order order);

    List<OrderResponseDto> toResponseDtoList(List<Order> orders);

    @Mapping(target = "itemId", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getId() : null)")
    @Mapping(target = "itemName", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getName() : null)")
    @Mapping(target = "itemPrice", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getPrice() : null)")
    OrderItemResponseDto toResponseDto(OrderItem orderItem);


    @AfterMapping
    default void calculateTotalPrice(@MappingTarget OrderResponseDto dto, Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            dto.setTotalPrice(BigDecimal.ZERO);
            return;
        }

        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotalPrice(total);
    }
}
