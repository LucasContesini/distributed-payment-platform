package com.finflow.auth.application;

import com.finflow.auth.application.command.LoginCommand;
import com.finflow.auth.application.command.RefreshTokenCommand;
import com.finflow.auth.application.command.RegisterUserCommand;
import com.finflow.auth.application.result.AuthResult;
import com.finflow.auth.domain.exception.AccountDisabledException;
import com.finflow.auth.domain.exception.InvalidCredentialsException;
import com.finflow.auth.domain.exception.TokenExpiredException;
import com.finflow.auth.domain.exception.UserAlreadyExistsException;
import com.finflow.auth.domain.token.RefreshToken;
import com.finflow.auth.domain.token.RefreshTokenRepository;
import com.finflow.auth.domain.user.User;
import com.finflow.auth.domain.user.UserRepository;
import com.finflow.auth.domain.user.UserRole;
import com.finflow.auth.domain.user.UserStatus;
import com.finflow.auth.infrastructure.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@Transactional
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthApplicationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResult register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        var user = User.create(
                command.email(),
                passwordEncoder.encode(command.password()),
                UserRole.USER
        );
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResult login(LoginCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountDisabledException();
        }

        return issueTokens(user);
    }

    public AuthResult refresh(RefreshTokenCommand command) {
        var tokenHash = sha256(command.refreshToken());

        var storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(TokenExpiredException::new);

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new TokenExpiredException();
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        var user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(TokenExpiredException::new);

        return issueTokens(user);
    }

    private AuthResult issueTokens(User user) {
        var accessToken = jwtTokenService.generateAccessToken(user);
        var rawRefresh = jwtTokenService.generateRefreshToken();

        var refreshToken = RefreshToken.create(user.getId(), sha256(rawRefresh));
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(accessToken, rawRefresh);
    }

    private String sha256(String input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
