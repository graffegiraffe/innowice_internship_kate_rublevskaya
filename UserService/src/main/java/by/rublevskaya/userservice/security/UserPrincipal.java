package by.rublevskaya.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String login;
    private String role;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isUser() {
        return "USER".equals(role);
    }
}