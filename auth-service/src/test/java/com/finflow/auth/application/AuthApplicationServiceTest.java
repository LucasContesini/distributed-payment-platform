package com.finflow.auth.application;

import com.finflow.auth.application.command.LoginCommand;
import com.finflow.auth.application.command.RefreshTokenCommand;
import com.finflow.auth.application.command.RegisterUserCommand;
import com.finflow.auth.domain.exception.InvalidCredentialsException;
import com.finflow.auth.domain.exception.TokenExpiredException;
import com.finflow.auth.domain.exception.UserAlreadyExistsException;
import com.finflow.auth.domain.token.RefreshToken;
import com.finflow.auth.domain.token.RefreshTokenRepository;
import com.finflow.auth.domain.user.User;
import com.finflow.auth.domain.user.UserRepository;
import com.finflow.auth.domain.user.UserRole;
import com.finflow.auth.infrastructure.security.JwtProperties;
import com.finflow.auth.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AuthApplicationService authService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        var jwtProperties = new JwtProperties("test-secret-key-at-least-32-characters-long", Duration.ofMinutes(15));
        var jwtTokenService = new JwtTokenService(jwtProperties);
        authService = new AuthApplicationService(userRepository, refreshTokenRepository, jwtTokenService, passwordEncoder);
    }

    @Test
    void register_WithNewEmail_ReturnsTokens() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = authService.register(new RegisterUserCommand("user@example.com", "Password123!"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ThrowsUserAlreadyExists() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterUserCommand("user@example.com", "Password123!"))
        ).isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void login_WithValidCredentials_ReturnsTokens() {
        var user = User.create("user@example.com", passwordEncoder.encode("Password123!"), UserRole.USER);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = authService.login(new LoginCommand("user@example.com", "Password123!"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void login_WithInvalidPassword_ThrowsInvalidCredentials() {
        var user = User.create("user@example.com", passwordEncoder.encode("correct-password"), UserRole.USER);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                authService.login(new LoginCommand("user@example.com", "wrong-password"))
        ).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_WithUnknownEmail_ThrowsInvalidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(new LoginCommand("nobody@example.com", "password"))
        ).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_WithExpiredToken_ThrowsTokenExpired() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.refresh(new RefreshTokenCommand("some-random-token"))
        ).isInstanceOf(TokenExpiredException.class);
    }
}
