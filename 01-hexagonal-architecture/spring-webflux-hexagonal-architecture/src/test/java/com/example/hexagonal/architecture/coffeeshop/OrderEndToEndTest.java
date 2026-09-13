package com.example.hexagonal.architecture.coffeeshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrderEndToEndTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void fullLifecycleAgainstTheRealDatabase() {
        JsonNode created = webTestClient.post().uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"location":"TAKE_AWAY","items":[{"drink":"LATTE","milk":"SOY","size":"MEDIUM","quantity":2}]}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();

        String id = created.get("id").asString();

        webTestClient.get().uri("/orders/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("PAYMENT_EXPECTED");

        webTestClient.post().uri("/orders/{id}/pay", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("PAID");

        webTestClient.post().uri("/orders/{id}/prepare", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("PREPARING");

        webTestClient.post().uri("/orders/{id}/ready", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("READY");

        webTestClient.post().uri("/orders/{id}/take", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("TAKEN");

        // A taken order can no longer be paid again — real business rule, real HTTP status.
        webTestClient.post().uri("/orders/{id}/pay", id)
                .exchange()
                .expectStatus().is4xxClientError();

        webTestClient.get().uri("/orders/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }
}
