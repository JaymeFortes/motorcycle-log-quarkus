package org.acme.services;

import org.acme.dtos.LoginRequest;
import org.acme.dtos.LoginResponse;
import org.acme.dtos.RegisterRequest;
import org.acme.dtos.UserResponse;
import org.acme.models.User;
import org.acme.repositories.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.PasswordCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Regras de negocio de autenticacao. O resource (AuthResource) so traduz
 * HTTP <-> chamadas destes dois metodos.
 */
@ApplicationScoped
public class AuthService {

    // Vira o claim "iss" no JWT emitido pelo login. Precisa ser IDENTICO ao
    // mp.jwt.verify.issuer em application.properties: e o que o Quarkus usa
    // para validar o token nas rotas protegidas - se os dois valores
    // divergirem, todo login passa a gerar um token que a propria app rejeita.
    private static final String ISSUER = "https://motolog.local/issuer";
    private static final long TOKEN_EXPIRES_IN_SECONDS = 3600;

    @Inject
    UserRepository userRepository;

    // Bean gerenciado pelo Quarkus Security. E ele quem sabe, em tempo de
    // execucao, que existe um JpaIdentityProvider (gerado pela anotacao
    // @UserDefinition em User) capaz de autenticar um par usuario/senha.
    @Inject
    IdentityProviderManager identityProviderManager;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            // 409: e-mail ja cadastrado. WebApplicationException lancada aqui
            // vira resposta HTTP direto, sem precisar de ExceptionMapper
            // dedicado - o proprio JAX-RS usa o status embutido na excecao.
            throw new WebApplicationException("E-mail ja cadastrado", Response.Status.CONFLICT);
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        // bcryptHash gera salt aleatorio e devolve o hash no formato que
        // @Password (em User) espera encontrar na coluna password_hash.
        user.setPassword_hash(BcryptUtil.bcryptHash(request.password()));

        userRepository.persist(user);

        return UserResponse.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        // UsernamePasswordAuthenticationRequest e o "pedido de autenticacao"
        // generico do Quarkus Security; o IdentityProviderManager despacha
        // esse pedido para o provider certo - no nosso caso, o
        // JpaIdentityProvider, que busca o User pelo @Username (email) e
        // compara a senha com o hash em @Password usando BCrypt por baixo
        // dos panos. Ou seja: nao comparamos senha na mao aqui.
        var authRequest = new UsernamePasswordAuthenticationRequest(
                request.email(), new PasswordCredential(request.password().toCharArray()));

        SecurityIdentity identity;
        try {
            // authenticateBlocking (em vez de authenticate, que e reativo/Uni)
            // porque este metodo roda num worker thread comum do JAX-RS
            // classico - nao precisamos de programacao reativa aqui.
            identity = identityProviderManager.authenticateBlocking(authRequest);
        } catch (AuthenticationFailedException e) {
            // Credenciais invalidas (e-mail nao existe OU senha errada).
            // Mensagem generica de proposito: nao revela qual dos dois errou.
            throw new WebApplicationException("Credenciais invalidas", Response.Status.UNAUTHORIZED);
        }

        String token = Jwt.issuer(ISSUER)
                // getPrincipal().getName() = valor do campo @Username = email.
                .subject(identity.getPrincipal().getName())
                // getRoles() ja vem populado pelo JpaIdentityProvider a partir
                // do campo @Roles da entidade User.
                .groups(identity.getRoles())
                .expiresIn(TOKEN_EXPIRES_IN_SECONDS)
                // sign() sem argumentos usa a chave configurada em
                // smallrye.jwt.sign.key.location (application.properties).
                .sign();

        return new LoginResponse(token, TOKEN_EXPIRES_IN_SECONDS);
    }
}
