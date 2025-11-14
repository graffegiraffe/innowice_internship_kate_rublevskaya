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

    OrderResponseDto toResponseDto(Order order);

    List<OrderResponseDto> toResponseDtoList(List<Order> orders);

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.price", target = "itemPrice")
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
