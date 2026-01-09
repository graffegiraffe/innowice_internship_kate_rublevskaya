package by.rublevskaya.orderservice.service;

import by.rublevskaya.orderservice.client.UserServiceClient;
import by.rublevskaya.orderservice.dto.OrderItemRequestDto;
import by.rublevskaya.orderservice.dto.OrderRequestDto;
import by.rublevskaya.orderservice.dto.OrderResponseDto;
import by.rublevskaya.orderservice.dto.UserDto;
import by.rublevskaya.orderservice.exception.ResourceNotFoundException;
import by.rublevskaya.orderservice.mapper.OrderMapper;
import by.rublevskaya.orderservice.model.Item;
import by.rublevskaya.orderservice.model.Order;
import by.rublevskaya.orderservice.model.OrderStatus;
import by.rublevskaya.orderservice.repository.ItemRepository;
import by.rublevskaya.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final String BEARER_TOKEN = "Bearer token";

    @Test
    void createOrder() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setUserId(1L);
        OrderItemRequestDto itemRequest = new OrderItemRequestDto();
        itemRequest.setItemId(100L);
        itemRequest.setQuantity(2);
        requestDto.setItems(List.of(itemRequest));

        Item item = new Item();
        item.setId(100L);
        item.setPrice(BigDecimal.TEN);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUserId(1L);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("kate@test.com");

        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setId(1L);

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(userServiceClient.getUserById(1L, BEARER_TOKEN)).thenReturn(userDto);
        when(orderMapper.toResponseDto(savedOrder)).thenReturn(responseDto);
        OrderResponseDto result = orderService.createOrder(requestDto, BEARER_TOKEN);

        assertNotNull(result);
        assertEquals(userDto, result.getUser());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_NotFound() {
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemRequest = new OrderItemRequestDto();
        itemRequest.setItemId(999L);
        requestDto.setItems(List.of(itemRequest));

        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(requestDto, BEARER_TOKEN)
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(5L);

        UserDto userDto = new UserDto();
        userDto.setId(5L);
        OrderResponseDto responseDto = new OrderResponseDto();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(5L, BEARER_TOKEN)).thenReturn(userDto);
        when(orderMapper.toResponseDto(order)).thenReturn(responseDto);

        OrderResponseDto result = orderService.getOrderById(orderId, BEARER_TOKEN);
        assertNotNull(result);
        assertEquals(userDto, result.getUser());
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderById(1L, BEARER_TOKEN)
        );
    }

    @Test
    void getOrdersByIds() {
        List<Long> ids = List.of(1L, 2L);
        Order order1 = new Order(); order1.setUserId(1L);
        Order order2 = new Order(); order2.setUserId(2L);

        when(orderRepository.findAllByIdIn(ids)).thenReturn(List.of(order1, order2));
        when(orderMapper.toResponseDto(any(Order.class))).thenReturn(new OrderResponseDto());
        when(userServiceClient.getUserById(anyLong(), eq(BEARER_TOKEN))).thenReturn(new UserDto());

        List<OrderResponseDto> result = orderService.getOrdersByIds(ids, BEARER_TOKEN);
        assertEquals(2, result.size());
        verify(userServiceClient, times(2)).getUserById(anyLong(), eq(BEARER_TOKEN));
    }

    @Test
    void getOrdersByStatuses() {
        List<OrderStatus> statuses = List.of(OrderStatus.PENDING);
        Order order = new Order();
        order.setUserId(1L);

        when(orderRepository.findAllByStatusIn(statuses)).thenReturn(List.of(order));
        when(orderMapper.toResponseDto(order)).thenReturn(new OrderResponseDto());
        when(userServiceClient.getUserById(1L, BEARER_TOKEN)).thenReturn(new UserDto());

        List<OrderResponseDto> result = orderService.getOrdersByStatuses(statuses, BEARER_TOKEN);
        assertEquals(1, result.size());
    }

    @Test
    void updateOrder() {
        Long orderId = 1L;
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setUserId(2L);
        requestDto.setItems(Collections.emptyList());

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setUserId(1L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
        when(orderMapper.toResponseDto(existingOrder)).thenReturn(new OrderResponseDto());
        when(userServiceClient.getUserById(2L, BEARER_TOKEN)).thenReturn(new UserDto());

        OrderResponseDto result = orderService.updateOrder(orderId, requestDto, BEARER_TOKEN);
        assertNotNull(result);
        assertEquals(2L, existingOrder.getUserId());
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void updateOrderStatus() {
        Long orderId = 1L;
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponseDto(order)).thenReturn(new OrderResponseDto());
        when(userServiceClient.getUserById(1L, BEARER_TOKEN)).thenReturn(new UserDto());

        OrderResponseDto result = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED, BEARER_TOKEN);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void deleteOrderById_Success() {
        Long orderId = 1L;
        Order order = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(orderId);
        verify(orderRepository).delete(order);
    }
}