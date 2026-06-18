package com.nexora.core.presentation.rest.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.core.presentation.rest.user.dto.UserResponse;
import com.nexora.core.application.user.usecases.queries.GetUserByIdUseCase;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints para la gestión y consulta de usuarios")
public class UserController {

    private final GetUserByIdUseCase getUserByIdUseCase;

    @Operation(summary = "Obtener usuario por ID", description = "Recupera la información detallada de un usuario dado su identificador único (UUID).")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse userResponse = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(userResponse);
    }
    
}

