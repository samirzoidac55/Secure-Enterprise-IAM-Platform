package com.samir.iam.util.mapper;

import com.samir.iam.dto.UserRequest;
import com.samir.iam.dto.UserResponse;
import com.samir.iam.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleMapper roleMapper;

    public User toEntity(UserRequest request) {
        if (request == null) return null;
        User entity = new User();
        entity.setUsername(request.getUsername());
        entity.setEmail(request.getEmail());
        entity.setPassword(request.getPassword()); // Note: Password encoding should be handled in Service
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        return entity;
    }

    public UserResponse toResponse(User entity) {
        if (entity == null) return null;
        return UserResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .enabled(entity.isEnabled())
                .accountLocked(entity.isAccountLocked())
                .emailVerified(entity.isEmailVerified())
                .lastLogin(entity.getLastLogin())
                .roles(entity.getRoles().stream()
                        .map(roleMapper::toResponse)
                        .collect(Collectors.toSet()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(User entity, UserRequest request) {
        if (request == null || entity == null) return;
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        // Username and Email updates usually require more complex logic/validation
    }
}
