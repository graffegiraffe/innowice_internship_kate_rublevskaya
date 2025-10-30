package by.rublevskaya.userservice.integration;

import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.dto.user.UserResponse;
import by.rublevskaya.userservice.entity.User;
import by.rublevskaya.userservice.exception.EmailAlreadyExistsException;
import by.rublevskaya.userservice.exception.UserNotFoundException;
import by.rublevskaya.userservice.repository.UserRepository;
import by.rublevskaya.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRequest = new UserRequest();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        userRequest.setEmail("test@example.com");
    }

    @Test
    void createUser() {
        UserResponse response = userService.createUser(userRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(userRequest.getName(), response.getName());
        assertEquals(userRequest.getEmail(), response.getEmail());

        Optional<User> savedUser = userRepository.findById(response.getId());
        assertTrue(savedUser.isPresent());
        assertEquals(userRequest.getEmail(), savedUser.get().getEmail());
    }

    @Test
    void createUserWithExistingEmail() {
        userService.createUser(userRequest);
        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(userRequest));
    }

    @Test
    void getUserById() {
        UserResponse createdUser = userService.createUser(userRequest);
        UserResponse foundUser = userService.getUserById(createdUser.getId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void getUserByIdWithNonExistingId() {
        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void getUserByEmail() {
        userService.createUser(userRequest);
        UserResponse foundUser = userService.getUserByEmail(userRequest.getEmail());

        assertNotNull(foundUser);
        assertEquals(userRequest.getEmail(), foundUser.getEmail());
    }

    @Test
    void getAllUsers() {
        userService.createUser(userRequest);

        UserRequest userRequest2 = new UserRequest();
        userRequest2.setName("kate");
        userRequest2.setSurname("rublevskaya");
        userRequest2.setEmail("rublevskaya@example.com");
        userService.createUser(userRequest2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<UserResponse> users = userService.getAllUsers(pageable);

        assertNotNull(users);
        assertEquals(2, users.getTotalElements());
        assertEquals(2, users.getContent().size());
    }

    @Test
    void updateUser_ShouldUpdateUserSuccessfully() {
        UserResponse createdUser = userService.createUser(userRequest);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated");
        updateRequest.setSurname("User");
        updateRequest.setEmail("updated@example.com");

        UserResponse updatedUser = userService.updateUser(createdUser.getId(), updateRequest);

        assertNotNull(updatedUser);
        assertEquals("Updated", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());

        Optional<User> dbUser = userRepository.findById(createdUser.getId());
        assertTrue(dbUser.isPresent());
        assertEquals("Updated", dbUser.get().getName());
    }

    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() {
        UserResponse createdUser = userService.createUser(userRequest);
        userService.deleteUser(createdUser.getId());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(createdUser.getId()));

        Optional<User> deletedUser = userRepository.findById(createdUser.getId());
        assertFalse(deletedUser.isPresent());
    }
}