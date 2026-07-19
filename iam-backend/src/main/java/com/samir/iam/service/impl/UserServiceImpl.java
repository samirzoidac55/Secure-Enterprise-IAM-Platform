package com.samir.iam.service.impl;

import com.samir.iam.dto.UserRequest;
import com.samir.iam.dto.UserResponse;
import com.samir.iam.entity.Role;
import com.samir.iam.entity.User;
import com.samir.iam.exception.BusinessException;
import com.samir.iam.exception.DuplicateResourceException;
import com.samir.iam.exception.ResourceNotFoundException;
import com.samir.iam.repository.RoleRepository;
import com.samir.iam.repository.UserRepository;
import com.samir.iam.service.UserService;
import com.samir.iam.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.info("Creating user with username: {} and email: {}", request.getUsername(), request.getEmail());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        // TODO: Password encoding (BCrypt) will be added with Spring Security
        
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size()) {
                throw new ResourceNotFoundException("One or more roles not found");
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userMapper.updateEntity(user, request);
        
        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.setRoles(roles);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public UserResponse assignRole(UUID userId, UUID roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        if (user.getRoles().contains(role)) {
            throw new BusinessException("Role is already assigned to this user");
        }

        user.getRoles().add(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse removeRole(UUID userId, UUID roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        if (!user.getRoles().contains(role)) {
            throw new BusinessException("Role is not assigned to this user");
        }

        user.getRoles().remove(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void enableUser(UUID id) {
        log.info("Enabling user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disableUser(UUID id) {
        log.info("Disabling user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyEmail(UUID id) {
        log.info("Verifying email for user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void recordLastLogin(String username) {
        log.info("Recording last login for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}
