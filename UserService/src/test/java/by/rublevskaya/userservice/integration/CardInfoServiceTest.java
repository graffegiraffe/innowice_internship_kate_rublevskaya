package by.rublevskaya.userservice.integration;

import by.rublevskaya.userservice.dto.card.CardRequest;
import by.rublevskaya.userservice.dto.card.CardResponse;
import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.dto.user.UserResponse;
import by.rublevskaya.userservice.entity.CardInfo;
import by.rublevskaya.userservice.exception.CardNotFoundException;
import by.rublevskaya.userservice.exception.CardNumberExistsException;
import by.rublevskaya.userservice.exception.UserNotFoundException;
import by.rublevskaya.userservice.repository.CardInfoRepository;
import by.rublevskaya.userservice.service.CardInfoService;
import by.rublevskaya.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardInfoServiceTest extends BaseTest {

    @Autowired
    private CardInfoService cardInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private CardInfoRepository cardInfoRepository;

    private UserResponse testUser;
    private CardRequest cardRequest;

    private String generateUniqueEmail() {
        return "test." + UUID.randomUUID() + "@example.com";
    }

    @BeforeEach
    void setUp() {
        cardInfoRepository.deleteAll();
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Card");
        userRequest.setSurname("Owner");
        userRequest.setEmail(generateUniqueEmail());
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        testUser = userService.createUser(userRequest);

        cardRequest = new CardRequest();
        cardRequest.setUserId(testUser.getId());
        cardRequest.setNumber("1234567812345678");
        cardRequest.setHolder("CARD OWNER");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(2));
    }

    @Test
    void createCard() {
        CardResponse response = cardInfoService.createCard(cardRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(cardRequest.getNumber(), response.getNumber());
        assertEquals(testUser.getId(), response.getUserId());
        assertNotNull(response.getUserName());

        List<CardInfo> userCards = cardInfoRepository.findCardsByUserId(testUser.getId());
        assertEquals(1, userCards.size());
        assertEquals(cardRequest.getNumber(), userCards.get(0).getNumber());
    }

    @Test
    void createCardWithNonExistingUse() {
        cardRequest.setUserId(999L);
        assertThrows(UserNotFoundException.class,
                () -> cardInfoService.createCard(cardRequest));
    }

    @Test
    void createCard_WithDuplicateNumber() {
        cardInfoService.createCard(cardRequest);
        assertThrows(CardNumberExistsException.class,
                () -> cardInfoService.createCard(cardRequest));
    }

    @Test
    void getCardById() {
        CardResponse createdCard = cardInfoService.createCard(cardRequest);
        CardResponse foundCard = cardInfoService.getCardById(createdCard.getId());
        assertNotNull(foundCard);
        assertEquals(createdCard.getId(), foundCard.getId());
        assertEquals(createdCard.getNumber(), foundCard.getNumber());
    }

    @Test
    void getCardsByUserId() {
        cardInfoService.createCard(cardRequest);

        CardRequest cardRequest2 = new CardRequest();
        cardRequest2.setUserId(testUser.getId());
        cardRequest2.setNumber("8765432187654321");
        cardRequest2.setHolder("CARD OWNER");
        cardInfoService.createCard(cardRequest2);

        List<CardResponse> userCards = cardInfoService.getCardsByUserId(testUser.getId());
        assertNotNull(userCards);
        assertEquals(2, userCards.size());
    }

    @Test
    void getAllCards() {
        cardInfoService.createCard(cardRequest);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<CardResponse> cards = cardInfoService.getAllCards(pageable);
        assertNotNull(cards);
        assertEquals(1, cards.getTotalElements());
    }

    @Test
    void updateCard() {
        CardResponse createdCard = cardInfoService.createCard(cardRequest);

        CardRequest updateRequest = new CardRequest();
        updateRequest.setUserId(testUser.getId());
        updateRequest.setNumber("9999888877776666");
        updateRequest.setHolder("UPDATED HOLDER");

        CardResponse updatedCard = cardInfoService.updateCard(createdCard.getId(), updateRequest);

        assertNotNull(updatedCard);
        assertEquals("9999888877776666", updatedCard.getNumber());
        assertEquals("UPDATED HOLDER", updatedCard.getHolder());
    }

    @Test
    void updateCardWithNewUser() {
        CardResponse createdCard = cardInfoService.createCard(cardRequest);

        UserRequest newUserRequest = new UserRequest();
        newUserRequest.setName("New");
        newUserRequest.setSurname("Owner");
        newUserRequest.setEmail(generateUniqueEmail());
        UserResponse newUser = userService.createUser(newUserRequest);

        CardRequest updateRequest = new CardRequest();
        updateRequest.setUserId(newUser.getId());
        updateRequest.setNumber(createdCard.getNumber());
        updateRequest.setHolder(createdCard.getHolder());

        CardResponse updatedCard = cardInfoService.updateCard(createdCard.getId(), updateRequest);

        assertNotNull(updatedCard);
        assertEquals(newUser.getId(), updatedCard.getUserId());
        assertEquals(newUser.getName() + " " + newUser.getSurname(), updatedCard.getUserName());
    }

    @Test
    void deleteCard() {
        CardResponse createdCard = cardInfoService.createCard(cardRequest);
        cardInfoService.deleteCard(createdCard.getId());
        assertThrows(CardNotFoundException.class,
                () -> cardInfoService.getCardById(createdCard.getId()));
    }

    @Test
    void cacheIntegration() {
        CardResponse createdCard = cardInfoService.createCard(cardRequest);
        CardResponse card1 = cardInfoService.getCardById(createdCard.getId());
        CardResponse card2 = cardInfoService.getCardById(createdCard.getId());
        assertEquals(card1.getId(), card2.getId());
        assertEquals(card1.getNumber(), card2.getNumber());
    }
}