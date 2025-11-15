package by.rublevskaya.orderservice.controller;

import by.rublevskaya.orderservice.dto.OrderRequestDto;
import by.rublevskaya.orderservice.dto.OrderResponseDto;
import by.rublevskaya.orderservice.model.OrderStatus;
import by.rublevskaya.orderservice.service.OrderService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto requestDto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String bearerToken) {
        OrderResponseDto createdOrder = orderService.createOrder(requestDto, bearerToken);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long id, @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(orderService.getOrderById(id, bearerToken));
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByIds(
            @RequestParam List<Long> ids, @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(orderService.getOrdersByIds(ids, bearerToken));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatuses(
            @RequestParam List<OrderStatus> statuses, @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(orderService.getOrdersByStatuses(statuses, bearerToken));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestDto requestDto, @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(orderService.updateOrder(id, requestDto, bearerToken));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate, @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken) {

        OrderStatus newStatus = OrderStatus.valueOf(statusUpdate.get("status"));
        return ResponseEntity.ok(orderService.updateOrderStatus(id, newStatus, bearerToken));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
    }
}
