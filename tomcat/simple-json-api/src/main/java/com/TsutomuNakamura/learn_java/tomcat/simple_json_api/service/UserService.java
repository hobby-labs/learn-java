package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.repository.UserRepository;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.exception.UserNotFoundException;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.exception.InvalidDataException;
import java.util.List;

public class UserService {
    private final UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new UserRepository();
    }
    
    // Constructor for dependency injection (if needed for testing)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) throws UserNotFoundException, InvalidDataException {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Invalid user ID");
        }
        
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }
    
    public User createUser(User user) throws InvalidDataException {
        validateUser(user);
        user.setId(null); // Ensure ID is null for new users
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, User user) throws UserNotFoundException, InvalidDataException {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Invalid user ID");
        }
        
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        
        validateUser(user);
        user.setId(id);
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) throws UserNotFoundException, InvalidDataException {
        if (id == null || id <= 0) {
            throw new InvalidDataException("Invalid user ID");
        }
        
        if (!userRepository.deleteById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
    }
    
    private void validateUser(User user) throws InvalidDataException {
        if (user == null) {
            throw new InvalidDataException("User data cannot be null");
        }
        
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new InvalidDataException("User name is required");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new InvalidDataException("User email is required");
        }
        
        if (!isValidEmail(user.getEmail())) {
            throw new InvalidDataException("Invalid email format");
        }
        
        if (user.getAge() < 0 || user.getAge() > 150) {
            throw new InvalidDataException("User age must be between 0 and 150");
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
