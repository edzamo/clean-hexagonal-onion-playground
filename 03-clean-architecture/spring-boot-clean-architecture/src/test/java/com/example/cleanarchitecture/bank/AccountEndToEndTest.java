package com.example.cleanarchitecture.bank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code TestRestTemplate}/{@code WebTestClient} aren't available yet as
 * separate artifacts in this Spring Boot 4.1.1 release, so this uses the
 * JDK's own {@code java.net.http.HttpClient} against the real running
 * server instead — framework-agnostic, no missing-module risk.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountEndToEndTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl() + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void fullLifecycleAgainstTheRealDatabase() throws Exception {
        JsonNode ana = objectMapper.readTree(post("/accounts", "{\"holderName\":\"Ana\"}").body());
        JsonNode beto = objectMapper.readTree(post("/accounts", "{\"holderName\":\"Beto\"}").body());
        UUID anaId = UUID.fromString(ana.get("id").asString());
        UUID betoId = UUID.fromString(beto.get("id").asString());

        HttpResponse<String> depositResponse = post("/accounts/" + anaId + "/deposit", "{\"amount\":100.00}");
        assertThat(depositResponse.statusCode()).isEqualTo(200);
        JsonNode afterDeposit = objectMapper.readTree(depositResponse.body());
        assertThat(afterDeposit.get("balance").decimalValue()).isEqualByComparingTo("100.00");

        HttpResponse<String> balanceResponse = get("/accounts/" + anaId + "/balance");
        assertThat(balanceResponse.statusCode()).isEqualTo(200);
        JsonNode balance = objectMapper.readTree(balanceResponse.body());
        assertThat(balance.get("formattedBalance").asString()).isEqualTo("$100.00");

        HttpResponse<String> transferResponse = post(
                "/accounts/" + anaId + "/transfer",
                "{\"targetAccountId\":\"" + betoId + "\",\"amount\":40.00}");
        assertThat(transferResponse.statusCode()).isEqualTo(200);
        JsonNode transfer = objectMapper.readTree(transferResponse.body());
        assertThat(transfer.get("sourceAccount").get("balance").decimalValue()).isEqualByComparingTo("60.00");
        assertThat(transfer.get("targetAccount").get("balance").decimalValue()).isEqualByComparingTo("40.00");

        // Overdrawing is a real business rule violation, not a framework error.
        HttpResponse<String> overdraw = post("/accounts/" + anaId + "/withdraw", "{\"amount\":1000.00}");
        assertThat(overdraw.statusCode()).isEqualTo(409);

        HttpResponse<String> notFound = get("/accounts/" + UUID.randomUUID() + "/balance");
        assertThat(notFound.statusCode()).isEqualTo(404);
    }
}
