package com.nexora.core.presentation.rest.user.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private Boolean isActive;
    private String role;
}
