package com.developer.test.dto;

import com.developer.test.model.Task;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TasksResponse {
    @JsonProperty("tasks")
    private List<Task> tasks;
    
    @JsonProperty("count")
    private int count;

    public TasksResponse() {
    }

    public TasksResponse(List<Task> tasks, int count) {
        this.tasks = tasks;
        this.count = count;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TasksResponse that = (TasksResponse) o;
        return count == that.count && java.util.Objects.equals(tasks, that.tasks);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(tasks, count);
    }
}
