package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.repository;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }
    
    public User save(User user) {
        if (user.getId() == null) {
            // Generate new ID for new users
            Long newId = users.stream()
                    .mapToLong(User::getId)
                    .max()
                    .orElse(0L) + 1;
            user.setId(newId);
            users.add(user);
        } else {
            // Update existing user
            Optional<User> existingUser = findById(user.getId());
            if (existingUser.isPresent()) {
                User existing = existingUser.get();
                existing.setName(user.getName());
                existing.setEmail(user.getEmail());
                existing.setAge(user.getAge());
                return existing;
            }
        }
        return user;
    }
    
    public boolean deleteById(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }
    
    public boolean existsById(Long id) {
        return users.stream().anyMatch(user -> user.getId().equals(id));
    }
}
