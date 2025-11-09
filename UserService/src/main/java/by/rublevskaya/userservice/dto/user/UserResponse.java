package by.rublevskaya.userservice.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private List<CardSummary> cards;

    @Data
    public static class CardSummary {
        private Long id;
        private String number;
        private String holder;
        private LocalDate expirationDate;
    }
}