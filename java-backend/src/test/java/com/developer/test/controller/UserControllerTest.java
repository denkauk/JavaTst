package com.developer.test.controller;

import com.developer.test.dto.UsersResponse;
import com.developer.test.model.User;
import com.developer.test.service.DataStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DataStore dataStore;

    @Test
    void testGetUsers() {
        List<User> users = Arrays.asList(
                new User(1, "User 1", "user1@example.com", "developer"),
                new User(2, "User 2", "user2@example.com", "designer")
        );
        UsersResponse expectedResponse = new UsersResponse(users, users.size());

        when(dataStore.getUsers()).thenReturn(users);

        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UsersResponse.class)
                .isEqualTo(expectedResponse);
    }

    @Test
    void testGetUserById() {
        User user = new User(1, "User 1", "user1@example.com", "developer");

        when(dataStore.getUserById(1)).thenReturn(user);

        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(user);
    }

    @Test
    void testGetUserById_NotFound() {
        when(dataStore.getUserById(999)).thenReturn(null);

        webTestClient.get()
                .uri("/api/users/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateUser() {
        User user = new User(0, "New User", "new@example.com", "developer");
        User savedUser = new User(4, "New User", "new@example.com", "developer");

        when(dataStore.createUser(any(User.class))).thenReturn(savedUser);

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .isEqualTo(savedUser);
    }

    @Test
    void testCreateUserInvalidRequest() {
        User user = new User(0, "", "invalid-email", "");

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
