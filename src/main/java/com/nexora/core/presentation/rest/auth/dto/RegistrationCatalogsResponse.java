package com.nexora.core.presentation.rest.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RegistrationCatalogsResponse {
    private List<String> careers;
    private List<String> academicInterests;
}