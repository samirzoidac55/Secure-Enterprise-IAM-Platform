package com.samir.iam.util.mapper;

import com.samir.iam.dto.RoleRequest;
import com.samir.iam.dto.RoleResponse;
import com.samir.iam.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public Role toEntity(RoleRequest request) {
        if (request == null) return null;
        Role entity = new Role();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return entity;
    }

    public RoleResponse toResponse(Role entity) {
        if (entity == null) return null;
        return RoleResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .permissions(entity.getPermissions().stream()
                        .map(permissionMapper::toResponse)
                        .collect(Collectors.toSet()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(Role entity, RoleRequest request) {
        if (request == null || entity == null) return;
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
    }
}
