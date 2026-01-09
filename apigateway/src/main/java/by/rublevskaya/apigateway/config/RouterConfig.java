package by.rublevskaya.apigateway.config;

import by.rublevskaya.apigateway.dto.RegistrationRequestDto;
import by.rublevskaya.apigateway.handler.RegistrationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/auth/register",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = RegistrationHandler.class,
                    beanMethod = "register",
                    operation = @Operation(
                            operationId = "registerUser",
                            summary = "Register new user",
                            description = "Orchestrates registration across User Service and Auth Service with rollback capability",
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(implementation = RegistrationRequestDto.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Registration successful"),
                                    @ApiResponse(responseCode = "400", description = "Registration failed")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> authRoutes(RegistrationHandler registrationHandler) {
        return route(POST("/auth/register").and(accept(MediaType.APPLICATION_JSON)),
                registrationHandler::register);
    }
}