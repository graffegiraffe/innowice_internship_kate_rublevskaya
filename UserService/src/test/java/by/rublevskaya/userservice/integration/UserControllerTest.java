package by.rublevskaya.userservice.integration;

import by.rublevskaya.userservice.dto.user.UserRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateUniqueEmail() {
        return "test." + UUID.randomUUID() + "@test.com";
    }

    private UserRequest createUserRequest(String email) {
        UserRequest request = new UserRequest();
        request.setName("Integration");
        request.setSurname("Test");
        request.setBirthDate(LocalDate.of(1995, 5, 15));
        request.setEmail(email);
        return request;
    }

    private Long createUserAndGetId(UserRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("id").asLong();
    }

    @Test
    void createUser() throws Exception {
        UserRequest userRequest = createUserRequest(generateUniqueEmail());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(userRequest.getName()))
                .andExpect(jsonPath("$.email").value(userRequest.getEmail()));
    }

    @Test
    void createUserWithInvalidData() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setName("Test");
        invalidRequest.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void getUserById() throws Exception {
        String uniqueEmail = generateUniqueEmail();
        UserRequest userRequest = createUserRequest(uniqueEmail);
        Long userId = createUserAndGetId(userRequest);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userRequest.getName()))
                .andExpect(jsonPath("$.email").value(uniqueEmail));
    }

    @Test
    void updateUser() throws Exception {
        String uniqueEmail = generateUniqueEmail();
        UserRequest userRequest = createUserRequest(uniqueEmail);
        Long userId = createUserAndGetId(userRequest);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated");
        updateRequest.setSurname("User");
        updateRequest.setBirthDate(LocalDate.of(1995, 5, 15));
        updateRequest.setEmail("updated_" + UUID.randomUUID() + "@test.com");

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.email").value(updateRequest.getEmail()));
    }

    @Test
    void createUserWithDuplicateEmail() throws Exception {
        String duplicateEmail = generateUniqueEmail();
        UserRequest firstRequest = createUserRequest(duplicateEmail);
        createUserAndGetId(firstRequest);

        UserRequest duplicateRequest = createUserRequest(duplicateEmail);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email '" + duplicateEmail + "' is already taken"));
    }

    @Test
    void getUserByIdWithNonExistentId() throws Exception {
        Long nonExistentId = 999L;
        mockMvc.perform(get("/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserWithNonExistentId() throws Exception {
        Long nonExistentId = 999L;
        UserRequest updateRequest = createUserRequest(generateUniqueEmail());
        mockMvc.perform(put("/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }
}