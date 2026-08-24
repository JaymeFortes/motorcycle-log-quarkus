package org.acme;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class AuthResourceTest {

    // E-mail unico por teste (%s = System.nanoTime()) para nao colidir com o
    // teste de "e-mail duplicado", que precisa que o mesmo e-mail ja exista.
    private String randomEmail() {
        return "user" + System.nanoTime() + "@motolog.test";
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
        String body = """
                {"name":"Piloto","email":"%s","password":"segredo123"}
                """.formatted(email);

        given().contentType(ContentType.JSON).body(body)
                .when().post("/auth/register")
                .then().statusCode(201);

        given().contentType(ContentType.JSON).body(body)
                .when().post("/auth/register")
                .then().statusCode(409);
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

        given().contentType(ContentType.JSON)
                .body("""
                        {"name":"Piloto","email":"%s","password":"%s"}
                        """.formatted(email, password))
                .when().post("/auth/register")
                .then().statusCode(201);

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

        given().contentType(ContentType.JSON)
                .body("""
                        {"name":"Piloto","email":"%s","password":"segredo123"}
                        """.formatted(email))
                .when().post("/auth/register")
                .then().statusCode(201);

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
}
