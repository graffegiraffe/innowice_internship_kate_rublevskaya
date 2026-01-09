package by.rublevskaya.orderservice.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String login;
    private String role;
}