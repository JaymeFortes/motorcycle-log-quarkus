package org.acme.repositories;

import java.util.Optional;

import org.acme.models.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * PanacheRepository<User> ja da de graca metodos como persist()/listAll();
 * so precisamos acrescentar a consulta especifica que o AuthService usa para
 * checar e-mail duplicado no cadastro (o login nao passa por aqui - quem
 * busca e compara a senha e o JpaIdentityProvider, via
 * IdentityProviderManager).
 */
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByTelegramChatId(Long chatId) {
        return find("telegramChatId", chatId).firstResultOptional();
    }
}
