package by.rublevskaya.userservice.service.impl;

import by.rublevskaya.userservice.dto.user.UserRequest;
import by.rublevskaya.userservice.dto.user.UserResponse;
import by.rublevskaya.userservice.entity.User;
import by.rublevskaya.userservice.exception.EmailAlreadyExistsException;
import by.rublevskaya.userservice.exception.UserNotFoundException;
import by.rublevskaya.userservice.mapper.UserMapper;
import by.rublevskaya.userservice.repository.UserRepository;
import by.rublevskaya.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    private static final String USER_CACHE = "users";
    private static final String USER_EMAIL_CACHE = "usersByEmail";
    private static final String USERS_PAGE_CACHE = "usersPage";

    @Override
    @Transactional
    @CacheEvict(value = USERS_PAGE_CACHE, allEntries = true)
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating new user with email: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }

        User user = userMapper.toEntity(userRequest);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_CACHE, key = "#id")
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USERS_PAGE_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_EMAIL_CACHE, key = "#email")
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USERS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = USER_EMAIL_CACHE, key = "#userRequest.email")
    }, put = {
            @CachePut(value = USER_CACHE, key = "#id")
    })
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Updating user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existingUser.getEmail().equals(userRequest.getEmail())) {
            evictUserByEmailCache(existingUser.getEmail());
        }
        if (!existingUser.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }

        userMapper.updateEntityFromRequest(userRequest, existingUser);
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with id: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_CACHE, key = "#id"),
            @CacheEvict(value = USERS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = USER_EMAIL_CACHE, allEntries = true)
    })
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        evictUserByEmailCache(user.getEmail());
        evictUserCardsCache(id);
        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }

    private void evictUserByEmailCache(String email) {
        try {
            var cache = cacheManager.getCache(USER_EMAIL_CACHE);
            if (cache != null) {
                cache.evict(email);
                log.debug("Evicted user with email: {} from cache", email);
            }
        } catch (Exception e) {
            log.warn("Failed to evict user with email: {} from cache", email, e);
        }
    }

    private void evictUserCardsCache(Long userId) {
        try {
            var cache = cacheManager.getCache("userCards");
            if (cache != null) {
                cache.evict(userId);
                log.debug("Evicted cards cache for user id: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cards cache for user id: {}", userId, e);
        }
    }
}