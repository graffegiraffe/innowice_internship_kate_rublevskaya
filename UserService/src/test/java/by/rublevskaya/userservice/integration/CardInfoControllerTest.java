package by.rublevskaya.userservice.integration;

import by.rublevskaya.userservice.dto.card.CardRequest;
import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.repository.CardInfoRepository;
import by.rublevskaya.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CardInfoControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardInfoRepository cardInfoRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() throws Exception {
        cardInfoRepository.deleteAll();
        userRepository.deleteAll();

        UserRequest userRequest = new UserRequest();
        userRequest.setName("Card");
        userRequest.setSurname("Test");
        userRequest.setEmail("cardtest" + System.currentTimeMillis() + "@example.com");

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        testUserId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createCard() throws Exception {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setUserId(testUserId);
        cardRequest.setNumber("1111222233334444");
        cardRequest.setHolder("CARD HOLDER");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(3));

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").value(cardRequest.getNumber()))
                .andExpect(jsonPath("$.userId").value(testUserId))
                .andExpect(jsonPath("$.userName").exists());
    }

    @Test
    void getCardsByUserId() throws Exception {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setUserId(testUserId);
        cardRequest.setNumber("5555666677778888");
        cardRequest.setHolder("TEST HOLDER");

        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequest)));

        mockMvc.perform(get("/cards/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(testUserId));
    }
}