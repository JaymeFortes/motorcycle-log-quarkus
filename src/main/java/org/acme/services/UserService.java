package org.acme.services;

import java.util.List;

import org.acme.models.User;
import org.acme.repositories.UserRepository;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Ponto unico para resolver "quem esta autenticado" nas rotas protegidas.
 * O @RolesAllowed do resource ja garante que existe um JWT valido antes
 * mesmo do metodo rodar - o que falta e traduzir esse JWT pra um User de
 * verdade do banco, e e isso que getAuthenticatedUser() faz.
 */
@ApplicationScoped
public class UserService {

    // Mesmo tipo (SecurityIdentity) usado em AuthService.login, so que aqui
    // e injetado pronto pelo quarkus-smallrye-jwt a partir do header
    // "Authorization: Bearer ..." da requisicao atual - nao somos nos que
    // o construimos, e o mecanismo de seguranca do Quarkus.
    @Inject
    SecurityIdentity identity;

    @Inject
    UserRepository userRepository;

    /**
     * Nunca aceite um id/e-mail vindo do corpo da requisicao para decidir de
     * quem e um recurso (moto, manutencao, etc.) - use sempre este metodo.
     * Assim o dono e sempre o usuario do token, nunca um valor que o
     * cliente poderia forjar mandando o id de outra pessoa no JSON.
     */
    public User getAuthenticatedUser() {
        // getPrincipal().getName() = claim "sub" do JWT = e-mail (foi isso
        // que AuthService.login gravou com .subject(email) ao emitir o token).
        String email = identity.getPrincipal().getName();

        return userRepository.findByEmail(email)
                // Um JWT valido prova que o token foi assinado pela nossa
                // chave, mas nao prova que a conta ainda existe - o usuario
                // pode ter sido removido depois do token ser emitido, e o
                // token continua "valido" (assinatura ok, nao expirou) ate
                // o fim do seu prazo. Por isso ainda checamos aqui.
                .orElseThrow(() -> new WebApplicationException("Usuario nao encontrado", Response.Status.UNAUTHORIZED));
    }

    public User getUserById(Long id) {
        // findById (sem "Optional" no nome) devolve o User direto ou null;
        // findByIdOptional e a variante do Panache que ja embrulha isso num
        // Optional, entao da pra manter o mesmo .orElseThrow(...) que usamos
        // em getAuthenticatedUser() acima.
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Usuario nao encontrado", Response.Status.NOT_FOUND));
    }

    public List<User> getAllUsers() {
        return userRepository.listAll();
    }
}
