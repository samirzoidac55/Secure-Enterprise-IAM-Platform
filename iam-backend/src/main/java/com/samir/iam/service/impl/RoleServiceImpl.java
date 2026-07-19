package com.samir.iam.service.impl;

import com.samir.iam.dto.RoleRequest;
import com.samir.iam.dto.RoleResponse;
import com.samir.iam.entity.Permission;
import com.samir.iam.entity.Role;
import com.samir.iam.exception.BusinessException;
import com.samir.iam.exception.DuplicateResourceException;
import com.samir.iam.exception.ResourceNotFoundException;
import com.samir.iam.repository.PermissionRepository;
import com.samir.iam.repository.RoleRepository;
import com.samir.iam.service.RoleService;
import com.samir.iam.util.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating role with name: {}", request.getName());
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Role already exists with name: " + request.getName());
        }

        Role role = roleMapper.toEntity(request);
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            if (permissions.size() != request.getPermissionIds().size()) {
                throw new ResourceNotFoundException("One or more permissions not found");
            }
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        log.info("Role created with ID: {}", savedRole.getId());
        return roleMapper.toResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, RoleRequest request) {
        log.info("Updating role with ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        roleRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("Another role already exists with name: " + request.getName());
                    }
                });

        roleMapper.updateEntity(role, request);

        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponse(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        log.info("Deleting role with ID: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        // Check if role is assigned to any users (implementation dependent, usually handled by FK constraints)
        roleRepository.delete(role);
    }

    @Override
    public RoleResponse findById(UUID id) {
        return roleRepository.findById(id)
                .map(roleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
    }

    @Override
    public RoleResponse findByName(String name) {
        return roleRepository.findByName(name)
                .map(roleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    @Override
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponse assignPermission(UUID roleId, UUID permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));

        if (role.getPermissions().contains(permission)) {
            throw new BusinessException("Permission is already assigned to this role");
        }

        role.getPermissions().add(permission);
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse removePermission(UUID roleId, UUID permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));

        if (!role.getPermissions().contains(permission)) {
            throw new BusinessException("Permission is not assigned to this role");
        }

        role.getPermissions().remove(permission);
        return roleMapper.toResponse(roleRepository.save(role));
    }
}
