package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.repository;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final List<User> users = new ArrayList<>();
    
    static {
        // Initialize with sample data
        users.add(new User(1L, "John Doe", "john@example.com", 30));
        users.add(new User(2L, "Jane Smith", "jane@example.com", 25));
        users.add(new User(3L, "Bob Johnson", "bob@example.com", 35));
    }
    
    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}
