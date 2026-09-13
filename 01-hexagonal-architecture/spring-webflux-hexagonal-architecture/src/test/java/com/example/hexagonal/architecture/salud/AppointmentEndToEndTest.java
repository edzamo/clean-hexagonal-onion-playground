package com.example.hexagonal.architecture.salud;

import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AppointmentEndToEndTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void fullLifecycleAgainstTheRealDatabase() {
        String patientId = UUID.randomUUID().toString();
        String practitionerId = UUID.randomUUID().toString();

        JsonNode created = webTestClient.post().uri("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(("""
                        {"patientId":"%s","practitionerId":"%s","start":"2026-10-01T10:00:00","end":"2026-10-01T10:30:00","reason":"Chequeo"}
                        """).formatted(patientId, practitionerId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();

        String id = created.get("id").asString();

        webTestClient.get().uri("/appointments/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("REQUESTED");

        webTestClient.post().uri("/appointments/{id}/confirm", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("CONFIRMED");

        webTestClient.post().uri("/appointments/{id}/start", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("IN_PROGRESS");

        webTestClient.post().uri("/appointments/{id}/complete", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("COMPLETED");

        // A completed appointment can no longer be cancelled — real business rule, real HTTP status.
        webTestClient.post().uri("/appointments/{id}/cancel", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"reasonDescription\":\"tarde\"}")
                .exchange()
                .expectStatus().is4xxClientError();

        webTestClient.get().uri("/appointments/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }
}
