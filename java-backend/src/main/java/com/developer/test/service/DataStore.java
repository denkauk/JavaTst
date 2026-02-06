package com.developer.test.service;

import com.developer.test.model.Task;
import com.developer.test.model.User;
import com.developer.test.dto.StatsResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DataStore {
    private final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger nextUserId = new AtomicInteger(1);
    private final AtomicInteger nextTaskId = new AtomicInteger(1);

    public DataStore() {
        // Initialize with sample data
        users.put(1, new User(1, "John Doe", "john@example.com", "developer"));
        users.put(2, new User(2, "Jane Smith", "jane@example.com", "designer"));
        users.put(3, new User(3, "Bob Johnson", "bob@example.com", "manager"));
        
        tasks.put(1, new Task(1, "Implement authentication", Task.Status.PENDING, 1));
        tasks.put(2, new Task(2, "Design user interface", Task.Status.IN_PROGRESS, 2));
        tasks.put(3, new Task(3, "Review code changes", Task.Status.COMPLETED, 3));
        
        nextUserId.set(4);
        nextTaskId.set(4);
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUserById(int id) {
        return users.get(id);
    }

    public List<Task> getTasks(String status, String userId) {
        List<Task> allTasks = new ArrayList<>(tasks.values());
        
        return allTasks.stream()
                .filter(task -> {
                    boolean matchStatus = status == null || status.isEmpty();
                    boolean matchUserId = userId == null || userId.isEmpty() || 
                            task.getUserId() == Integer.parseInt(userId);
                    return matchStatus && matchUserId;
                })
                .collect(Collectors.toList());
    }

    public StatsResponse getStats() {
        StatsResponse stats = new StatsResponse();
        stats.getUsers().setTotal(users.size());
        stats.getTasks().setTotal(tasks.size());
        
        for (Task task : tasks.values()) {
            switch (task.getStatus()) {
                case PENDING:
                    stats.getTasks().setPending(stats.getTasks().getPending() + 1);
                    break;
                case IN_PROGRESS:
                    stats.getTasks().setInProgress(stats.getTasks().getInProgress() + 1);
                    break;
                case COMPLETED:
                    stats.getTasks().setCompleted(stats.getTasks().getCompleted() + 1);
                    break;
            }
        }
        
        return stats;
    }

    public User createUser(User user) {
        int id = nextUserId.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    public Task createTask(Task task) {
        int id = nextTaskId.getAndIncrement();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    public Task updateTask(int id, Task updated) {
        Task existing = tasks.get(id);
        if (existing == null) {
            return null;
        }
        if (updated.getTitle() != null) {
            existing.setTitle(updated.getTitle());
        }
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getUserId() != 0) {
            existing.setUserId(updated.getUserId());
        }
        return existing;
    }
}
