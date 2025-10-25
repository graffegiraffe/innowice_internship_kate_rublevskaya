package by.rublevskaya.userservice.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardRequest {

    @NotNull(message = "User ID is mandatory")
    private Long userId;

    @NotBlank(message = "Card number is mandatory")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    private String number;

    private String holder;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}