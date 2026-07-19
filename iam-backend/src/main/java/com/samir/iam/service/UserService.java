package com.samir.iam.service;

import com.samir.iam.dto.UserRequest;
import com.samir.iam.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(UUID id, UserRequest request);
    void deleteUser(UUID id);
    UserResponse findById(UUID id);
    List<UserResponse> findAll();
    UserResponse findByUsername(String username);
    UserResponse findByEmail(String email);
    UserResponse assignRole(UUID userId, UUID roleId);
    UserResponse removeRole(UUID userId, UUID roleId);
    void enableUser(UUID id);
    void disableUser(UUID id);
    void verifyEmail(UUID id);
    void recordLastLogin(String username);
}
