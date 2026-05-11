package com.finflow.auth.infrastructure.persistence;

import com.finflow.auth.domain.token.RefreshToken;
import com.finflow.auth.domain.token.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository delegate;

    RefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return delegate.findByTokenHash(tokenHash);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return delegate.save(token);
    }

    @Override
    public void deleteExpiredTokens() {
        delegate.deleteExpired();
    }
}
