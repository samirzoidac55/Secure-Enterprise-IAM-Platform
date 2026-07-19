package com.samir.iam.service;

import com.samir.iam.dto.RoleRequest;
import com.samir.iam.dto.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleResponse createRole(RoleRequest request);
    RoleResponse updateRole(UUID id, RoleRequest request);
    void deleteRole(UUID id);
    RoleResponse findById(UUID id);
    RoleResponse findByName(String name);
    List<RoleResponse> findAll();
    RoleResponse assignPermission(UUID roleId, UUID permissionId);
    RoleResponse removePermission(UUID roleId, UUID permissionId);
}
