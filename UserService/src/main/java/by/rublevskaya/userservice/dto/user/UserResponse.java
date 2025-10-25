package by.rublevskaya.userservice.dto.user;

import by.rublevskaya.userservice.dto.card.CardResponse;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private List<CardResponse> cards;
}