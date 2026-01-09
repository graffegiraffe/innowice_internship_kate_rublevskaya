package by.rublevskaya.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequestDto {
    @NotNull
    private Long itemId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
