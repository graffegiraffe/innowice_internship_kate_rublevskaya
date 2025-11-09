package by.rublevskaya.userservice.controller;

import by.rublevskaya.userservice.dto.card.CardRequest;
import by.rublevskaya.userservice.dto.card.CardResponse;
import by.rublevskaya.userservice.service.CardInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Card management API")
public class CardInfoController {

    private final CardInfoService cardInfoService;

    @Operation(
            summary = "Create a new card",
            description = "Creates a new card for a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Card created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Card number already exists for this user")
    })
    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest) {
        CardResponse createdCard = cardInfoService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @Operation(
            summary = "Get card by ID",
            description = "Retrieves a card by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card found"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long id) {
        CardResponse card = cardInfoService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @Operation(
            summary = "Get all cards",
            description = "Retrieves a paginated list of all cards"
    )
    @ApiResponse(responseCode = "200", description = "Cards retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<CardResponse> cards = cardInfoService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @Operation(
            summary = "Get cards by user ID",
            description = "Retrieves all cards associated with a specific user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardResponse>> getCardsByUserId(@PathVariable Long userId) {
        List<CardResponse> cards = cardInfoService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @Operation(
            summary = "Update card",
            description = "Updates an existing card's information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "409", description = "Card number already exists for this user")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardRequest cardRequest) {

        CardResponse updatedCard = cardInfoService.updateCard(id, cardRequest);
        return ResponseEntity.ok(updatedCard);
    }

    @Operation(
            summary = "Delete card",
            description = "Deletes a card by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardInfoService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}