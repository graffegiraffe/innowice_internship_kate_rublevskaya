package by.rublevskaya.userservice.service;

import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserByEmail(String email);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
}