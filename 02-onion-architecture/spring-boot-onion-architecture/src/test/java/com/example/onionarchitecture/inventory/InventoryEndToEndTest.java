package com.example.onionarchitecture.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestRestTemplate/WebTestClient aren't available yet as separate artifacts
 * in this Spring Boot 4.1.1 release — same finding as in the Clean
 * Architecture project. The JDK's own HttpClient sidesteps that entirely.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryEndToEndTest {

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
        JsonNode product = objectMapper.readTree(
                post("/products", "{\"sku\":\"SKU-E2E\",\"name\":\"Coffee\"}").body());
        UUID productId = UUID.fromString(product.get("id").asString());

        HttpResponse<String> afterReceive = post("/products/" + productId + "/stock/receive", "{\"quantity\":50}");
        assertThat(afterReceive.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(afterReceive.body()).get("quantityOnHand").asInt()).isEqualTo(50);

        HttpResponse<String> afterReserve = post("/products/" + productId + "/stock/reserve", "{\"quantity\":20}");
        assertThat(afterReserve.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(afterReserve.body()).get("quantityOnHand").asInt()).isEqualTo(30);

        // Insufficient stock is a real business rule violation, not a framework error.
        HttpResponse<String> overReserve = post("/products/" + productId + "/stock/reserve", "{\"quantity\":1000}");
        assertThat(overReserve.statusCode()).isEqualTo(409);

        // Can't discontinue with stock still on hand.
        HttpResponse<String> discontinueTooEarly = post("/products/" + productId + "/discontinue", "{}");
        assertThat(discontinueTooEarly.statusCode()).isEqualTo(409);

        post("/products/" + productId + "/stock/reserve", "{\"quantity\":30}");
        HttpResponse<String> discontinued = post("/products/" + productId + "/discontinue", "{}");
        assertThat(discontinued.statusCode()).isEqualTo(200);

        // The cross-aggregate rule enforced by the Domain Service.
        HttpResponse<String> reserveAfterDiscontinued = post(
                "/products/" + productId + "/stock/reserve", "{\"quantity\":1}");
        assertThat(reserveAfterDiscontinued.statusCode()).isEqualTo(409);

        HttpResponse<String> notFound = get("/products/" + UUID.randomUUID() + "/stock");
        assertThat(notFound.statusCode()).isEqualTo(404);
    }
}
