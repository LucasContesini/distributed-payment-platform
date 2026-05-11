package com.finflow.auth.api;

import com.finflow.auth.api.dto.AuthResponse;
import com.finflow.auth.api.dto.LoginRequest;
import com.finflow.auth.api.dto.RefreshTokenRequest;
import com.finflow.auth.api.dto.RegisterRequest;
import com.finflow.auth.application.AuthApplicationService;
import com.finflow.auth.application.command.LoginCommand;
import com.finflow.auth.application.command.RefreshTokenCommand;
import com.finflow.auth.application.command.RegisterUserCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        var result = authService.register(new RegisterUserCommand(request.email(), request.password()));
        return AuthResponse.from(result);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(new LoginCommand(request.email(), request.password()));
        return AuthResponse.from(result);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = authService.refresh(new RefreshTokenCommand(request.refreshToken()));
        return AuthResponse.from(result);
    }
}
