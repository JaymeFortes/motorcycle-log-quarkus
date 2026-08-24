package org.acme.controllers;

import org.acme.dtos.RegisterRequest;
import org.acme.dtos.UserResponse;
import org.acme.services.AuthService;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;

/**
 * @TestSecurity aqui simula direto o SecurityIdentity da requisicao HTTP -
 * nao precisa gerar um JWT de verdade nem ter um usuario "admin" real no
 * banco para testar a autorizacao (@RolesAllowed("admin") em UserResource).
 */
@QuarkusTest
class UserResourceTest {

    @Inject
    AuthService authService;

    private Long registerAndGetId(String email) {
        UserResponse response = authService.register(new RegisterRequest("Piloto", email, "segredo123"));
        return response.id();
    }

    @Test
    void getUserByIdWithoutTokenReturns401() {
        given()
                .when().get("/users/1")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "userresource-common@motolog.test", roles = "user")
    void getUserByIdAsRegularUserReturns403() {
        Long id = registerAndGetId("userresource-target1@motolog.test");

        given()
                .when().get("/users/" + id)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "userresource-admin@motolog.test", roles = "admin")
    void getUserByIdAsAdminReturns200WithTheUser() {
        String targetEmail = "userresource-target2@motolog.test";
        Long id = registerAndGetId(targetEmail);

        given()
                .when().get("/users/" + id)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.is(id.intValue()))
                .body("email", org.hamcrest.Matchers.is(targetEmail));
    }
}
