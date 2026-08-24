package org.acme.repositories;

import java.util.Optional;

import org.acme.models.PasswordResetToken;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordResetTokenRepository implements PanacheRepository<PasswordResetToken> {

    public Optional<PasswordResetToken> findByToken(String token) {
        return find("token", token).firstResultOptional();
    }
}
