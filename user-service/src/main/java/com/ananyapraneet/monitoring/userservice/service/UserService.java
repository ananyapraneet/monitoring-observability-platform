package com.ananyapraneet.monitoring.userservice.service;

import com.ananyapraneet.monitoring.userservice.dto.CreateUserRequest;
import com.ananyapraneet.monitoring.userservice.dto.UserResponse;
import com.ananyapraneet.monitoring.userservice.entity.User;
import com.ananyapraneet.monitoring.userservice.exception.DuplicateUserException;
import com.ananyapraneet.monitoring.userservice.exception.UserNotFoundException;
import com.ananyapraneet.monitoring.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.Counter;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final Counter userCreationCounter;

    public UserService(
            UserRepository userRepository,
            io.micrometer.core.instrument.MeterRegistry meterRegistry) {

        this.userRepository = userRepository;
        this.userCreationCounter = Counter.builder("user_creation_total")
                .description("Total number of users successfully created")
                .register(meterRegistry);
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException(
                    "User with this email already exists"
            );
        }

        User user = new User(request.name(), request.email());
        User savedUser = userRepository.save(user);
        userCreationCounter.increment();

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
