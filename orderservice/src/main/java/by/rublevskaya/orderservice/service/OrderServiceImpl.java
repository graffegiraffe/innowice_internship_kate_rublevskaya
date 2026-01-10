package by.rublevskaya.orderservice.service;

import by.rublevskaya.orderservice.client.UserServiceClient;
import by.rublevskaya.orderservice.dto.OrderRequestDto;
import by.rublevskaya.orderservice.dto.OrderResponseDto;
import by.rublevskaya.orderservice.dto.UserDto;
import by.rublevskaya.orderservice.event.OrderEvent;
import by.rublevskaya.orderservice.exception.ResourceNotFoundException;
import by.rublevskaya.orderservice.kafka.OrderProducer;
import by.rublevskaya.orderservice.mapper.OrderMapper;
import by.rublevskaya.orderservice.model.Item;
import by.rublevskaya.orderservice.model.Order;
import by.rublevskaya.orderservice.model.OrderItem;
import by.rublevskaya.orderservice.model.OrderStatus;
import by.rublevskaya.orderservice.repository.ItemRepository;
import by.rublevskaya.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final OrderProducer orderProducer;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String bearerToken) {
        Order order = new Order();
        order.setUserId(requestDto.getUserId());
        order.setStatus(OrderStatus.PENDING);
        addItemsToOrder(order, requestDto.getItems());
        Order savedOrder = orderRepository.save(order);

        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getOrderItems().stream()
                        .map(item -> item.getItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                "PENDING"
        );
        orderProducer.sendOrderEvent(event);
        return enrichOrderResponseWithUser(savedOrder, bearerToken);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id, String bearerToken) {
        Order order = findOrderById(id);
        return enrichOrderResponseWithUser(order, bearerToken);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByIds(List<Long> ids, String bearerToken) {
        List<Order> orders = orderRepository.findAllByIdIn(ids);
        return enrichOrderResponseListWithUsers(orders, bearerToken);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatuses(List<OrderStatus> statuses, String bearerToken) {
        List<Order> orders = orderRepository.findAllByStatusIn(statuses);
        return enrichOrderResponseListWithUsers(orders, bearerToken);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderRequestDto requestDto, String bearerToken) {
        Order order = findOrderById(id);
        order.setUserId(requestDto.getUserId());
        order.getOrderItems().clear();
        addItemsToOrder(order, requestDto.getItems());
        Order updatedOrder = orderRepository.save(order);

        return enrichOrderResponseWithUser(updatedOrder, bearerToken);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, OrderStatus newStatus, String bearerToken) {
        Order order = findOrderById(id);
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return enrichOrderResponseWithUser(updatedOrder, bearerToken);
    }

    @Override
    @Transactional
    public void deleteOrderById(Long id) {
        Order order = findOrderById(id);
        orderRepository.delete(order);
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private void addItemsToOrder(Order order, Collection<by.rublevskaya.orderservice.dto.OrderItemRequestDto> itemDtos) {
        for (by.rublevskaya.orderservice.dto.OrderItemRequestDto itemDto : itemDtos) {
            Item item = itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item not found with id: " + itemDto.getItemId()));
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(itemDto.getQuantity());
            order.addOrderItem(orderItem);
        }
    }

    private OrderResponseDto enrichOrderResponseWithUser(Order order, String bearerToken) {
        UserDto user = userServiceClient.getUserById(order.getUserId(), bearerToken);
        OrderResponseDto responseDto = orderMapper.toResponseDto(order);
        responseDto.setUser(user);
        return responseDto;
    }

    private List<OrderResponseDto> enrichOrderResponseListWithUsers(List<Order> orders, String bearerToken) {
        return orders.stream()
                .map(order -> enrichOrderResponseWithUser(order, bearerToken))
                .collect(Collectors.toList());
    }
}
