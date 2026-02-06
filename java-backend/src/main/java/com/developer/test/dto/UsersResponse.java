package com.developer.test.dto;

import com.developer.test.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class UsersResponse {
    @JsonProperty("users")
    private List<User> users;
    
    @JsonProperty("count")
    private int count;

    public UsersResponse() {
    }

    public UsersResponse(List<User> users, int count) {
        this.users = users;
        this.count = count;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
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
        UsersResponse that = (UsersResponse) o;
        return count == that.count && java.util.Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(users, count);
    }
}
