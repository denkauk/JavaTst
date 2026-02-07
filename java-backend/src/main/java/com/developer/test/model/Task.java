package com.developer.test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Task {
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("title")
    @NotBlank(message = "Name is required")
    private String title;
    
    @JsonProperty("status")
    @NotNull(message = "Status is required")
    private Status status;
    
    @JsonProperty("userId")
    private int userId;

    public enum Status {
        PENDING("pending"),
        IN_PROGRESS("in-progress"),
        COMPLETED("completed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public static List<String> allowed() {
            return Arrays.stream(values())
                    .map(Status::getValue)
                    .collect(Collectors.toList());
        }

        @JsonCreator
        public static Status from(String v) {
            for (Status s : values()) {
                if (s.value.equalsIgnoreCase(v)) return s;
            }
            throw new IllegalArgumentException("Invalid status: " + v);
        }
    }

    public Task() {
    }

    public Task(int id, String title, Status status, int userId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return userId == task.userId &&
                Objects.equals(title, task.title) &&
                Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, status, userId);
    }
}
