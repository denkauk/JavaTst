package com.developer.test.controller;

import com.developer.test.dto.TasksResponse;
import com.developer.test.model.Task;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DataStore dataStore;

    @Test
    void testGetTasks() {
        List<Task> tasks = Arrays.asList(
                new Task(1, "Task 1", Task.Status.PENDING, 1),
                new Task(2, "Task 2", Task.Status.COMPLETED, 2)
        );
        TasksResponse expectedResponse = new TasksResponse(tasks, tasks.size());

        when(dataStore.getTasks(null, null)).thenReturn(tasks);

        webTestClient.get()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TasksResponse.class)
                .isEqualTo(expectedResponse);
    }

    @Test
    void testCreateTask() {
        Task task = new Task(0, "New Task", Task.Status.PENDING, 1);
        Task savedTask = new Task(4, "New Task", Task.Status.PENDING, 1);

        when(dataStore.createTask(any(Task.class))).thenReturn(savedTask);

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(task), Task.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Task.class)
                .isEqualTo(savedTask);
    }

    @Test
    void testCreateTaskInvalidRequest() {
        Task task = new Task(0, "", Task.Status.PENDING, 1);

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(task), Task.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateTask() {
        Task updateInfo = new Task(0, "Updated Task", Task.Status.IN_PROGRESS, 1);
        Task updatedTask = new Task(1, "Updated Task", Task.Status.IN_PROGRESS, 1);

        when(dataStore.getUserById(1)).thenReturn(new com.developer.test.model.User());
        when(dataStore.updateTask(eq(1), any(Task.class))).thenReturn(updatedTask);

        webTestClient.put()
                .uri("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateInfo), Task.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Task.class)
                .isEqualTo(updatedTask);
    }

    @Test
    void testUpdateTaskNotFound() {
        Task updateInfo = new Task(0, "Updated Task", Task.Status.IN_PROGRESS, 1);

        when(dataStore.getUserById(1)).thenReturn(new com.developer.test.model.User());
        when(dataStore.updateTask(eq(999), any(Task.class))).thenReturn(null);

        webTestClient.put()
                .uri("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateInfo), Task.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateTaskInvalidUser() {
        Task updateInfo = new Task(0, "Updated Task", Task.Status.IN_PROGRESS, 999);

        when(dataStore.getUserById(999)).thenReturn(null);

        webTestClient.put()
                .uri("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateInfo), Task.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testCreateTaskInvalidStatus() {
        String invalidTaskJson = "{\"title\": \"Invalid Status Task\", \"status\": \"invalid-status\", \"userId\": 1}";

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTaskJson)
                .exchange()
                .expectStatus().isBadRequest();
    }
}