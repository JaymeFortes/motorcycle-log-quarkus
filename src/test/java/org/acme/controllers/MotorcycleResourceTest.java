package org.acme.controllers;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class MotorcycleResourceTest {

    private String randomEmail() {
        return "moto" + System.nanoTime() + "@motolog.test";
    }

    private String randomPlate() {
        return "P" + System.nanoTime();
    }

    // Fluxo real (register + login) em vez de @TestSecurity: aqui queremos
    // provar o caminho inteiro - @Authenticated validando o JWT de verdade,
    // nao so a autorizacao isolada como fizemos em UserServiceTest.
    private String registerAndLogin(String email, String password) {
        given().contentType(ContentType.JSON)
                .body("""
                        {"name":"Piloto","email":"%s","password":"%s"}
                        """.formatted(email, password))
                .when().post("/auth/register")
                .then().statusCode(201);

        return given().contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password))
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    private String createMotorcycleBody(String plate) {
        return """
                {"brand":"Honda","model":"CB 500","modelYear":2022,"plate":"%s","currentKm":1000,"currentEngineHours":50.5}
                """.formatted(plate);
    }

    @Test
    void createWithoutTokenReturns401() {
        given().contentType(ContentType.JSON)
                .body(createMotorcycleBody(randomPlate()))
                .when().post("/motorcycles")
                .then().statusCode(401);
    }

    @Test
    void listWithoutTokenReturns401() {
        given()
                .when().get("/motorcycles")
                .then().statusCode(401);
    }

    @Test
    void createReturns201WithTheOwnerFromTheToken() {
        String token = registerAndLogin(randomEmail(), "segredo123");
        String plate = randomPlate();

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createMotorcycleBody(plate))
                .when().post("/motorcycles")
                .then()
                .statusCode(201)
                .body("plate", is(plate))
                .body("ownerId", is(not(org.hamcrest.Matchers.nullValue())));
    }

    @Test
    void createWithDuplicatePlateReturns409EvenForAnotherUser() {
        String tokenA = registerAndLogin(randomEmail(), "segredo123");
        String tokenB = registerAndLogin(randomEmail(), "segredo123");
        String plate = randomPlate();

        given().header("Authorization", "Bearer " + tokenA)
                .contentType(ContentType.JSON)
                .body(createMotorcycleBody(plate))
                .when().post("/motorcycles")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + tokenB)
                .contentType(ContentType.JSON)
                .body(createMotorcycleBody(plate))
                .when().post("/motorcycles")
                .then().statusCode(409);
    }

    @Test
    void listReturnsOnlyMotorcyclesOwnedByTheAuthenticatedUser() {
        String tokenA = registerAndLogin(randomEmail(), "segredo123");
        String tokenB = registerAndLogin(randomEmail(), "segredo123");
        String plateA = randomPlate();

        given().header("Authorization", "Bearer " + tokenA)
                .contentType(ContentType.JSON)
                .body(createMotorcycleBody(plateA))
                .when().post("/motorcycles")
                .then().statusCode(201);

        // Dono ve a propria moto...
        given().header("Authorization", "Bearer " + tokenA)
                .when().get("/motorcycles")
                .then()
                .statusCode(200)
                .body("plate", hasItem(plateA));

        // ...mas outro usuario, autenticado com seu proprio token, nao ve
        // essa moto na lista dele. Essa e a prova concreta de "autorizacao
        // por dono" pedida no CLAUDE.md, nao so a criacao respeitando o dono.
        given().header("Authorization", "Bearer " + tokenB)
                .when().get("/motorcycles")
                .then()
                .statusCode(200)
                .body("plate", not(hasItem(plateA)));
    }
}
