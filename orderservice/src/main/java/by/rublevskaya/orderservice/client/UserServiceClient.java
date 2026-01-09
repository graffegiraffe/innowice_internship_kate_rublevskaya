package by.rublevskaya.orderservice.client;

import by.rublevskaya.orderservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient userServiceWebClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public UserDto getUserById(Long userId, String bearerToken) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        return circuitBreaker.run(
                () -> {
                    log.info("Calling User Service for user id: {}", userId);
                    return userServiceWebClient.get()
                            .uri("/users/{id}", userId)
                            .header("Authorization", bearerToken)
                            .retrieve()
                            .bodyToMono(UserDto.class)
                            .block();
                },
                throwable -> {
                    log.warn("User Service is down or returned error", throwable);
                    return getFallbackUser(userId);
                }
        );
    }

    private UserDto getFallbackUser(Long userId) {
        UserDto fallback = new UserDto();
        fallback.setId(userId);
        fallback.setName("User (Service Unavailable)");
        fallback.setSurname("");
        fallback.setEmail("user-not-found@example.com");
        return fallback;
    }
}