package by.rublevskaya.apigateway.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegistrationRequestDto {
    // UserService
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;

    // AuthService
    private String login;
    private String password;
    private String role;
}