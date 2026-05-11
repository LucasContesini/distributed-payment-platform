package com.finflow.auth.api.dto;

import com.finflow.auth.application.result.AuthResult;

public record AuthResponse(String accessToken, String refreshToken, String tokenType) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.accessToken(), result.refreshToken(), "Bearer");
    }
}
