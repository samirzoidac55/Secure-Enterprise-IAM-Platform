package com.samir.iam.service;

import com.samir.iam.dto.PermissionRequest;
import com.samir.iam.dto.PermissionResponse;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    PermissionResponse updatePermission(UUID id, PermissionRequest request);
    void deletePermission(UUID id);
    PermissionResponse findById(UUID id);
    PermissionResponse findByName(String name);
    List<PermissionResponse> findAll();
}
