package org.acme.services;

import org.acme.dtos.RegisterRequest;
import org.acme.models.User;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testa UserService injetando ele direto (sem passar por HTTP) - faz sentido
 * aqui porque nenhum endpoint usa UserService ainda. getAuthenticatedUser()
 * depende de SecurityIdentity, que normalmente so existe dentro de uma
 * requisicao HTTP autenticada; o @TestSecurity resolve isso simulando essa
 * identidade sem precisar de um JWT de verdade nem de um endpoint protegido.
 */
@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    // Reaproveita o AuthService (ja testado em AuthResourceTest) so para
    // criar usuarios de teste - evita duplicar a logica de hash de senha.
    @Inject
    AuthService authService;

    @Test
    @TestSecurity(user = "userservice-identity@motolog.test", roles = "user")
    void getAuthenticatedUserResolvesTheUserFromTheMockedIdentity() {
        // O e-mail aqui precisa ser IGUAL ao "user" do @TestSecurity acima:
        // e assim que getAuthenticatedUser() acha esse usuario, lendo
        // identity.getPrincipal().getName() - que o @TestSecurity fixa
        // nesse valor sem precisar de um JWT assinado de verdade.
        String email = "userservice-identity@motolog.test";
        authService.register(new RegisterRequest("Piloto Logado", email, "segredo123"));

        User authenticated = userService.getAuthenticatedUser();

        assertEquals(email, authenticated.getEmail());
    }

    @Test
    @TestSecurity(user = "usuario-sem-conta@motolog.test", roles = "user")
    void getAuthenticatedUserThrows401WhenTokenHasNoMatchingUser() {
        // Simula um token "valido" (o @TestSecurity garante isso) mas cujo
        // usuario nao existe no banco - ex.: conta deletada depois do token
        // ter sido emitido. Repare que aqui, de proposito, NAO registramos
        // esse e-mail antes de chamar o metodo.
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> userService.getAuthenticatedUser());

        assertEquals(401, exception.getResponse().getStatus());
    }
}
