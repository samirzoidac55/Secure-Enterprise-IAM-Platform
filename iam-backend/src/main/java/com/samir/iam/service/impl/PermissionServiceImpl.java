package com.samir.iam.service.impl;

import com.samir.iam.dto.PermissionRequest;
import com.samir.iam.dto.PermissionResponse;
import com.samir.iam.entity.Permission;
import com.samir.iam.exception.DuplicateResourceException;
import com.samir.iam.exception.ResourceNotFoundException;
import com.samir.iam.repository.PermissionRepository;
import com.samir.iam.service.PermissionService;
import com.samir.iam.util.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        log.info("Creating permission with name: {}", request.getName());
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Permission already exists with name: " + request.getName());
        }
        Permission permission = permissionMapper.toEntity(request);
        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission created with ID: {}", savedPermission.getId());
        return permissionMapper.toResponse(savedPermission);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(UUID id, PermissionRequest request) {
        log.info("Updating permission with ID: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + id));

        permissionRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("Another permission already exists with name: " + request.getName());
                    }
                });

        permissionMapper.updateEntity(permission, request);
        Permission updatedPermission = permissionRepository.save(permission);
        return permissionMapper.toResponse(updatedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(UUID id) {
        log.info("Deleting permission with ID: {}", id);
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found with ID: " + id);
        }
        // TODO: Check if permission is assigned to any roles before deletion
        permissionRepository.deleteById(id);
    }

    @Override
    public PermissionResponse findById(UUID id) {
        return permissionRepository.findById(id)
                .map(permissionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + id));
    }

    @Override
    public PermissionResponse findByName(String name) {
        return permissionRepository.findByName(name)
                .map(permissionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with name: " + name));
    }

    @Override
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }
}
