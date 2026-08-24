package org.acme;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthResourceTest {

    // Injetavel so porque quarkus.mailer.mock e o padrao fora de %prod - em
    // vez de mandar e-mail de verdade, o Mailer guarda as mensagens aqui.
    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void clearMailbox() {
        mailbox.clear();
    }

    // E-mail unico por teste (%s = System.nanoTime()) para nao colidir com o
    // teste de "e-mail duplicado", que precisa que o mesmo e-mail ja exista.
    private String randomEmail() {
        return "user" + System.nanoTime() + "@motolog.test";
    }

    private void register(String email, String password) {
        given().contentType(ContentType.JSON)
                .body("""
                        {"name":"Piloto","email":"%s","password":"%s"}
                        """.formatted(email, password))
                .when().post("/auth/register")
                .then().statusCode(201);
    }

    // O e-mail mockado carrega o token cru no corpo (ver AuthService.forgotPassword);
    // aqui so extraimos esse UUID com uma regex simples para usar no teste.
    private String extractToken(String emailBody) {
        Matcher matcher = Pattern.compile("[0-9a-f-]{36}").matcher(emailBody);
        assertTrue(matcher.find(), "token UUID nao encontrado no corpo do e-mail: " + emailBody);
        return matcher.group();
    }

    @Test
    void registerWithValidDataReturns201AndNeverExposesPasswordHash() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Piloto","email":"%s","password":"segredo123"}
                        """.formatted(randomEmail()))
                .when().post("/auth/register")
                .then()
                .statusCode(201)
                .body("id", is(not(blankOrNullString())))
                .body("$", not(org.hamcrest.Matchers.hasKey("password")))
                .body("$", not(org.hamcrest.Matchers.hasKey("password_hash")));
    }

    @Test
    void registerWithDuplicateEmailReturns409() {
        String email = randomEmail();

        register(email, "segredo123");

        given().contentType(ContentType.JSON)
                .body("""
                        {"name":"Piloto","email":"%s","password":"segredo123"}
                        """.formatted(email))
                .when().post("/auth/register")
                .then().statusCode(409);
    }

    @Test
    void forgotPasswordWithEmptyBodyReturns422NotServerError() {
        // Corpo vazio desserializa para um record null; sem @NotNull ao lado
        // do @Valid no resource, isso batia direto num NullPointerException
        // (500) dentro de AuthService, em vez do 422 esperado.
        given().contentType(ContentType.JSON)
                .body("")
                .when().post("/auth/forgot-password")
                .then().statusCode(422);
    }

    @Test
    void registerWithShortPasswordReturns422() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Piloto","email":"%s","password":"123"}
                        """.formatted(randomEmail()))
                .when().post("/auth/register")
                .then().statusCode(422);
    }

    @Test
    void loginWithValidCredentialsReturns200AndToken() {
        String email = randomEmail();
        String password = "segredo123";

        register(email, password);

        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password))
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", is(not(blankOrNullString())));
    }

    @Test
    void loginWithWrongPasswordReturns401() {
        String email = randomEmail();

        register(email, "segredo123");

        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"senhaErrada"}
                        """.formatted(email))
                .when().post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    void loginWithUnknownEmailReturns401() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"qualquercoisa"}
                        """.formatted(randomEmail()))
                .when().post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    void forgotPasswordForExistingEmailReturns200AndSendsEmailWithToken() {
        String email = randomEmail();
        register(email, "senhaAntiga1");

        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s"}
                        """.formatted(email))
                .when().post("/auth/forgot-password")
                .then().statusCode(200);

        List<Mail> sent = mailbox.getMailsSentTo(email);
        assertEquals(1, sent.size(), "esperava 1 e-mail de reset enviado (mockado)");
        String token = extractToken(sent.get(0).getText());

        given().contentType(ContentType.JSON)
                .body("""
                        {"token":"%s","newPassword":"senhaNova123"}
                        """.formatted(token))
                .when().post("/auth/reset-password")
                .then().statusCode(200);

        // Confirma que a troca teve efeito real: senha antiga para de funcionar...
        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"senhaAntiga1"}
                        """.formatted(email))
                .when().post("/auth/login")
                .then().statusCode(401);

        // ...e a nova senha loga normalmente.
        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"senhaNova123"}
                        """.formatted(email))
                .when().post("/auth/login")
                .then().statusCode(200);
    }

    @Test
    void forgotPasswordForUnknownEmailStillReturns200AndSendsNoEmail() {
        String email = randomEmail();

        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s"}
                        """.formatted(email))
                .when().post("/auth/forgot-password")
                .then().statusCode(200);

        // Nenhum usuario com esse e-mail existe, entao nenhum e-mail deve
        // ter sido "enviado" - e essa a prova de que a rota nao enumera
        // e-mails cadastrados (o status HTTP sozinho nao provaria isso).
        assertEquals(0, mailbox.getMailsSentTo(email).size());
    }

    @Test
    void resetPasswordWithInvalidTokenReturns400() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"token":"token-que-nao-existe","newPassword":"senhaNova123"}
                        """)
                .when().post("/auth/reset-password")
                .then().statusCode(400);
    }

    @Test
    void resetPasswordWithAlreadyUsedTokenReturns400() {
        String email = randomEmail();
        register(email, "senhaAntiga1");

        given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s"}
                        """.formatted(email))
                .when().post("/auth/forgot-password")
                .then().statusCode(200);

        String token = extractToken(mailbox.getMailsSentTo(email).get(0).getText());
        String resetBody = """
                {"token":"%s","newPassword":"senhaNova123"}
                """.formatted(token);

        // Primeiro uso: consome o token normalmente.
        given().contentType(ContentType.JSON).body(resetBody)
                .when().post("/auth/reset-password")
                .then().statusCode(200);

        // Segundo uso do MESMO token: precisa falhar, mesmo dentro da janela de 30 min.
        given().contentType(ContentType.JSON).body(resetBody)
                .when().post("/auth/reset-password")
                .then().statusCode(400);
    }
}
