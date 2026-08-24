package org.acme.controllers;

import org.acme.repositories.MaintenanceTypeRepository;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class MaintenanceResourceTest {

    // "Troca de oleo" vem do seed em import.sql: interval_km = 3000, sem
    // interval_engine_hours. Buscamos pelo nome em vez de fixar o id, pra
    // nao depender da ordem exata em que o import.sql insere as linhas.
    @Inject
    MaintenanceTypeRepository maintenanceTypeRepository;

    private String randomEmail() {
        return "maint" + System.nanoTime() + "@motolog.test";
    }

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

    private Long createMotorcycle(String token, int currentKm) {
        return given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"brand":"Honda","model":"CB 500","modelYear":2022,"plate":"P%s","currentKm":%d,"currentEngineHours":50.0}
                        """.formatted(System.nanoTime(), currentKm))
                .when().post("/motorcycles")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private void registerOilChange(String token, Long motorcycleId, int odometerKm) {
        Long oilChangeTypeId = maintenanceTypeRepository.find("name", "Troca de oleo").firstResult().getId();

        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"maintenanceTypeId":%d,"serviceDate":"2020-01-01T00:00:00.000Z","odometerKm":%d,"engineHours":50.0,"cost":100.0,"notes":"teste"}
                        """.formatted(oilChangeTypeId, odometerKm))
                .when().post("/motorcycles/" + motorcycleId + "/maintenances")
                .then().statusCode(201);
    }

    @Test
    void upcomingIsEmptyWhenThereIsNoMaintenanceHistory() {
        String token = registerAndLogin(randomEmail(), "segredo123");
        Long motorcycleId = createMotorcycle(token, 1000);

        // Sem nenhum registro de manutencao ainda, nao ha "ultima manutencao"
        // pra calcular a proxima - por decisao de projeto, isso e omitido da
        // lista, nao tratado como vencido.
        given().header("Authorization", "Bearer " + token)
                .when().get("/motorcycles/" + motorcycleId + "/maintenances/upcoming")
                .then()
                .statusCode(200)
                .body("$", empty());
    }

    @Test
    void upcomingIsEmptyWhenThereIsEnoughSlackLeft() {
        String token = registerAndLogin(randomEmail(), "segredo123");
        Long motorcycleId = createMotorcycle(token, 1000);
        registerOilChange(token, motorcycleId, 1000);

        // Troca de oleo em 1000km, intervalo 3000km -> proxima em 4000km.
        // Moto continua em 1000km: faltam 3000km, bem acima do limiar de
        // "proximo" (10% de 3000 = 300km). Nao deve aparecer na lista.
        given().header("Authorization", "Bearer " + token)
                .when().get("/motorcycles/" + motorcycleId + "/maintenances/upcoming")
                .then()
                .statusCode(200)
                .body("$", empty());
    }

    @Test
    void upcomingMarksAsNearWhenWithinTenPercentOfTheInterval() {
        String token = registerAndLogin(randomEmail(), "segredo123");
        Long motorcycleId = createMotorcycle(token, 1000);
        registerOilChange(token, motorcycleId, 1000);

        // Proxima troca prevista em 4000km. Avanca a moto pra 3800km ->
        // faltam 200km, dentro do limiar de 300km (10% de 3000) = "proximo".
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"currentKm":3800}
                        """)
                .when().patch("/motorcycles/" + motorcycleId)
                .then().statusCode(200);

        given().header("Authorization", "Bearer " + token)
                .when().get("/motorcycles/" + motorcycleId + "/maintenances/upcoming")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].maintenanceTypeName", is("Troca de oleo"))
                .body("[0].status", is("NEAR"))
                .body("[0].remainingKm", is(200));
    }

    @Test
    void upcomingMarksAsOverdueWhenPastTheInterval() {
        String token = registerAndLogin(randomEmail(), "segredo123");
        Long motorcycleId = createMotorcycle(token, 1000);
        registerOilChange(token, motorcycleId, 1000);

        // Proxima troca prevista em 4000km. Moto ja passou disso (4500km) ->
        // remainingKm negativo, status VENCIDO.
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"currentKm":4500}
                        """)
                .when().patch("/motorcycles/" + motorcycleId)
                .then().statusCode(200);

        given().header("Authorization", "Bearer " + token)
                .when().get("/motorcycles/" + motorcycleId + "/maintenances/upcoming")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].status", is("OVERDUE"))
                .body("[0].remainingKm", is(-500));
    }

    @Test
    void upcomingWithoutTokenReturns401() {
        given()
                .when().get("/motorcycles/1/maintenances/upcoming")
                .then().statusCode(401);
    }
}
