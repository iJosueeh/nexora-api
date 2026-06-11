package com.nexora.core.application.security.services;

import java.util.UUID;

public interface SecurityService {
    UUID getCurrentUserId();
    String getCurrentUserEmail();
}
