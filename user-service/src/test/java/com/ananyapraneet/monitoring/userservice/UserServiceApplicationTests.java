package com.ananyapraneet.monitoring.userservice;

import com.ananyapraneet.monitoring.userservice.entity.User;
import com.ananyapraneet.monitoring.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldCreateUser() throws Exception {
        String request = """
                {
                    "name": "John Doe",
                    "email": "john.doe@example.com"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetUserById() throws Exception {
        User user = new User("Jane Doe", "jane.doe@example.com");
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Jane Doe")))
                .andExpect(jsonPath("$.email", is("jane.doe@example.com")));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        userRepository.save(new User("John Doe", "john@example.com"));
        userRepository.save(new User("Jane Doe", "jane@example.com"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        userRepository.save(new User("Existing User", "existing@example.com"));

        String request = """
                {
                    "name": "Another User",
                    "email": "existing@example.com"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message",
                        is("User with this email already exists")));
    }

    @Test
    void shouldRejectInvalidUserRequest() throws Exception {
        String request = """
                {
                    "name": "",
                    "email": "invalid-email"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void shouldReturnNotFoundForMissingUser() throws Exception {
        mockMvc.perform(get("/users/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = new User("Delete Me", "delete@example.com");
        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", savedUser.getId()))
                .andExpect(status().isNotFound());
    }
}
