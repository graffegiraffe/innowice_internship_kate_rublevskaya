package by.rublevskaya.apigateway.handler;

import by.rublevskaya.apigateway.dto.RegistrationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class RegistrationHandler {

    private final WebClient webClient;
    private final String userServiceUrl;
    private final String authServiceUrl;

    public RegistrationHandler(WebClient.Builder webClientBuilder,
                               @Value("${urls.user-service}") String userServiceUrl,
                               @Value("${urls.auth-service}") String authServiceUrl) {
        this.webClient = webClientBuilder.build();
        this.userServiceUrl = userServiceUrl;
        this.authServiceUrl = authServiceUrl;
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegistrationRequestDto.class)
                .flatMap(dto -> {
                    return createUserInUserService(dto)
                            .flatMap(userResponse -> {
                                Long userId = extractId(userResponse);
                                log.info("User created in User Service with ID: {}", userId);

                                return createCredentialsInAuthService(dto, userId)
                                        .then(ServerResponse.status(HttpStatus.CREATED).bodyValue("User registered successfully"))

                                        .onErrorResume(e -> {
                                            log.error("Failed to create credentials. Rolling back user creation for ID: {}", userId);
                                            return rollbackUserCreation(userId)
                                                    .then(Mono.error(new RuntimeException("Registration failed, rollback executed: " + e.getMessage())));
                                        });
                            });
                })
                .onErrorResume(e -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage()));
    }

    private Mono<Map> createUserInUserService(RegistrationRequestDto dto) {
        Map<String, Object> userRequest = Map.of(
                "name", dto.getName(),
                "surname", dto.getSurname(),
                "birthDate", dto.getBirthDate().toString(),
                "email", dto.getEmail()
        );

        return webClient.post()
                .uri(userServiceUrl + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .retrieve()
                .bodyToMono(Map.class);
    }

    private Mono<Void> createCredentialsInAuthService(RegistrationRequestDto dto, Long userId) {
        Map<String, Object> authRequest = Map.of(
                "login", dto.getLogin(),
                "password", dto.getPassword(),
                "userId", userId,
                "role", dto.getRole()
        );

        return webClient.post()
                .uri(authServiceUrl + "/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Mono<Void> rollbackUserCreation(Long userId) {
        return webClient.delete()
                .uri(userServiceUrl + "/users/" + userId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Long extractId(Map userResponse) {
        Object idObj = userResponse.get("id");
        if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        } else if (idObj instanceof Long) {
            return (Long) idObj;
        }
        throw new IllegalArgumentException("Invalid ID format from User Service");
    }
}