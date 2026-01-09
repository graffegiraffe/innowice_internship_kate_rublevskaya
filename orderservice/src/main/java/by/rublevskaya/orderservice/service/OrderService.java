package by.rublevskaya.orderservice.service;

import by.rublevskaya.orderservice.dto.OrderRequestDto;
import by.rublevskaya.orderservice.dto.OrderResponseDto;
import by.rublevskaya.orderservice.model.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto requestDto, String bearerToken);

    OrderResponseDto getOrderById(Long id, String bearerToken);

    List<OrderResponseDto> getOrdersByIds(List<Long> ids, String bearerToken);

    List<OrderResponseDto> getOrdersByStatuses(List<OrderStatus> statuses, String bearerToken);

    OrderResponseDto updateOrder(Long id, OrderRequestDto requestDto, String bearerToken);

    OrderResponseDto updateOrderStatus(Long id, OrderStatus newStatus, String bearerToken);

    void deleteOrderById(Long id);
}