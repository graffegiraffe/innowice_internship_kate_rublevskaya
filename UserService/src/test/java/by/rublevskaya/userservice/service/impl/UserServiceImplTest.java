package by.rublevskaya.userservice.service.impl;

import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.dto.user.UserResponse;
import by.rublevskaya.userservice.entity.User;
import by.rublevskaya.userservice.exception.EmailAlreadyExistsException;
import by.rublevskaya.userservice.exception.UserNotFoundException;
import by.rublevskaya.userservice.mapper.UserMapper;
import by.rublevskaya.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;
    private User user;
    private UserResponse userResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setName("Kate");
        userRequest.setSurname("Rubl");
        userRequest.setBirthDate(LocalDate.of(2005, 11, 25));
        userRequest.setEmail("rubl@icloud.com");

        user = new User();
        user.setId(1L);
        user.setName("Kate");
        user.setSurname("Rubl");
        user.setBirthDate(LocalDate.of(2005, 11, 25));
        user.setEmail("rubl@icloud.com");
        user.setCards(Collections.emptyList());

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("Kate");
        userResponse.setSurname("Kate");
        userResponse.setBirthDate(LocalDate.of(2005, 11, 25));
        userResponse.setEmail("rubl@icloud.com");
        userResponse.setCards(Collections.emptyList());

        pageable = PageRequest.of(0, 10, Sort.by("id"));
        when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    @Test
    void createUser() {
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        UserResponse result = userService.createUser(userRequest);
        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserEmailExists() {
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(userRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        UserResponse result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals(userResponse, result);
    }

    @Test
    void getUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getAllUsers() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        Page<UserResponse> result = userService.getAllUsers(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userResponse, result.getContent().get(0));
    }

    @Test
    void getUserByEmail() {
        when(userRepository.findUserByEmail("rubl@icloud.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        UserResponse result = userService.getUserByEmail("rubl@icloud.com");
        assertNotNull(result);
        assertEquals(userResponse, result);
    }

    @Test
    void getUserByEmailNotFound() {
        when(userRepository.findUserByEmail("rubl@icloud.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("rubl@icloud.com"));
    }

    @Test
    void updateUser() {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated");
        updateRequest.setEmail("updatedrubl@icloud.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("updatedrubl@icloud.com", 1L)).thenReturn(false);
        doNothing().when(userMapper).updateEntityFromRequest(updateRequest, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, updateRequest);
        assertNotNull(result);
        verify(userRepository).save(user);
        verify(cache).evict("rubl@icloud.com");
    }

    @Test
    void updateUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, userRequest));
    }

    @Test
    void updateUserEmailExists() {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setEmail("rublevs@icloud.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("rublevs@icloud.com", 1L)).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(1L, updateRequest));
    }

    @Test
    void deleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
        verify(cache).evict("rubl@icloud.com");
    }

    @Test
    void deleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    }
}