package com.samir.iam.util.mapper;

import com.samir.iam.dto.PermissionRequest;
import com.samir.iam.dto.PermissionResponse;
import com.samir.iam.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission toEntity(PermissionRequest request) {
        if (request == null) return null;
        Permission entity = new Permission();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return entity;
    }

    public PermissionResponse toResponse(Permission entity) {
        if (entity == null) return null;
        return PermissionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(Permission entity, PermissionRequest request) {
        if (request == null || entity == null) return;
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
    }
}
