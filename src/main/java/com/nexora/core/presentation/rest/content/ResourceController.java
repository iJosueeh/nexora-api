package com.nexora.core.presentation.rest.content;

import com.nexora.core.application.content.usecases.resources.commands.UploadResourceUseCase;
import com.nexora.core.common.response.ApiResponse;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.presentation.rest.content.dto.UploadResourceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Recursos Académicos", description = "Endpoints para la gestión de recursos académicos")
public class ResourceController {

    private final UploadResourceUseCase uploadResourceUseCase;

    @Operation(summary = "Subir recurso académico", description = "Sube un archivo (PDF, EPUB, MD, PPTX, DOCX) con metadatos. Tamaño máximo: 20MB.")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<AcademicResource>> uploadResource(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file,
            @RequestPart("data") @Valid UploadResourceRequest request) {

        AcademicResource resource = uploadResourceUseCase.execute(
                file,
                request.getTitle(),
                request.getDescription(),
                request.getCategoryId(),
                request.getType()
        );

        return ResponseEntity.ok(ApiResponse.<AcademicResource>builder()
                .success(true)
                .message("Resource uploaded successfully")
                .data(resource)
                .build());
    }
}
