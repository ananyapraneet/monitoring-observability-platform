package com.ananyapraneet.monitoring.userservice.controller;

import com.ananyapraneet.monitoring.userservice.dto.CreateUserRequest;
import com.ananyapraneet.monitoring.userservice.dto.UserResponse;
import com.ananyapraneet.monitoring.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final Counter serviceRequestsCounter;

    public UserController(
            UserService userService,
            MeterRegistry meterRegistry) {

        this.userService = userService;

        this.serviceRequestsCounter = Counter.builder("service_requests_total")
                .description("Total number of requests handled by the User Service")
                .register(meterRegistry);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse createdUser = userService.createUser(request);

        serviceRequestsCounter.increment();

        return ResponseEntity
                .created(URI.create("/users/" + createdUser.id()))
                .body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        serviceRequestsCounter.increment();

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        serviceRequestsCounter.increment();

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        serviceRequestsCounter.increment();

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
