package com.developer.test.controller;

import com.developer.test.dto.TasksResponse;
import com.developer.test.service.DataStore;
import com.developer.test.model.Task;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final DataStore dataStore;
    
    public TaskController(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    
    @GetMapping
    public ResponseEntity<TasksResponse> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId) {
        List<com.developer.test.model.Task> tasks = dataStore.getTasks(status, userId);
        TasksResponse response = new TasksResponse(tasks, tasks.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public Mono<ResponseEntity<Task>> createTask(@Validated @RequestBody Task task) {
        return Mono.fromSupplier(() ->
                ResponseEntity.status(HttpStatus.CREATED).body(dataStore.createTask(task))
        );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Task>> updateTask(@PathVariable int id, @RequestBody Task task) {
        return Mono.fromSupplier(() -> {
            if (task.getUserId() != 0 && dataStore.getUserById(task.getUserId()) == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Task updated = dataStore.updateTask(id, task);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        });
    }
}
