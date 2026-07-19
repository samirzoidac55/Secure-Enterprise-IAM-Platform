package com.samir.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {
    @NotBlank(message = "Permission name is required")
    @Size(min = 3, max = 50, message = "Permission name must be between 3 and 50 characters")
    private String name;

    private String description;
}
