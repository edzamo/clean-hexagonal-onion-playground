package com.ezamora.coffeeshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Year;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.OrderRepository;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderStatus;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Flujo de negocio completo por HTTP contra H2 real: el estado sobrevive a cada recarga. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoffeeShopEndToEndTest {

    private static final String ORDER_BODY =
            "{\"location\":\"IN_STORE\",\"items\":[{\"drink\":\"LATTE\",\"milk\":\"WHOLE\",\"size\":\"LARGE\",\"quantity\":2}]}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
    }

    private UUID createOrder() throws Exception {
        var body = mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(ORDER_BODY))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asText());
    }

    private static String payBody(String pan, int year) {
        return "{\"cardHolderName\":\"Ana Perez\",\"cardNumber\":\"" + pan + "\",\"expiryMonth\":12,\"expiryYear\":" + year + "}";
    }

    private void assertPersistedStatus(UUID id, OrderStatus expected) {
        assertThat(orderRepository.findByUuid(id).orElseThrow().getStatus()).isEqualTo(expected);
    }

    @Test
    void orderGoesFromCreationToReceiptKeepingItsStateAcrossReloads() throws Exception {
        var id = createOrder();
        assertPersistedStatus(id, OrderStatus.PAYMENT_EXPECTED);

        mockMvc.perform(post("/order/{id}/pay", id).contentType(MediaType.APPLICATION_JSON)
                        .content(payBody("4111111111111111", Year.now().getValue() + 3)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.last4").value("1111"));
        assertPersistedStatus(id, OrderStatus.PAID);

        mockMvc.perform(put("/order/{id}/prepare/start", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
        assertPersistedStatus(id, OrderStatus.PREPARING);

        mockMvc.perform(put("/order/{id}/prepare/finish", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
        assertPersistedStatus(id, OrderStatus.READY);

        mockMvc.perform(put("/order/{id}/take", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TAKEN"));

        mockMvc.perform(get("/order/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.TAKEN.name()))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
        mockMvc.perform(get("/order/{id}/receipt", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(10.0));
    }

    @Test
    void payingWithAnExpiredCardIsUnprocessableAndLeavesTheOrderUnpaid() throws Exception {
        var id = createOrder();

        mockMvc.perform(post("/order/{id}/pay", id).contentType(MediaType.APPLICATION_JSON)
                        .content(payBody("4111111111111111", Year.now().getValue() - 1)))
                .andExpect(status().isUnprocessableEntity());

        assertPersistedStatus(id, OrderStatus.PAYMENT_EXPECTED);
    }

    @Test
    void payingTwiceIsAConflict() throws Exception {
        var id = createOrder();
        var good = payBody("4111111111111111", Year.now().getValue() + 3);
        mockMvc.perform(post("/order/{id}/pay", id).contentType(MediaType.APPLICATION_JSON).content(good))
                .andExpect(status().isOk());

        mockMvc.perform(post("/order/{id}/pay", id).contentType(MediaType.APPLICATION_JSON).content(good))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownOrderIs404() throws Exception {
        mockMvc.perform(get("/order/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void cancellingAnUnpaidOrderDeletesIt() throws Exception {
        var id = createOrder();

        mockMvc.perform(delete("/order/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/order/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void anUnpaidOrderCanBeReplacedWithPut() throws Exception {
        var id = createOrder();
        var replacement = "{\"location\":\"TAKE_AWAY\",\"items\":[{\"drink\":\"ESPRESSO\",\"milk\":\"SOY\",\"size\":\"SMALL\",\"quantity\":1}]}";

        mockMvc.perform(put("/order/{id}", id).contentType(MediaType.APPLICATION_JSON).content(replacement))
                .andExpect(status().isOk()).andExpect(jsonPath("$.location").value("TAKE_AWAY"));

        mockMvc.perform(get("/order/{id}", id)).andExpect(jsonPath("$.items[0].drink").value("ESPRESSO"));
    }

    @Test
    void receiptOfAnUnpaidOrderIs404() throws Exception {
        var id = createOrder();
        mockMvc.perform(get("/order/{id}/receipt", id)).andExpect(status().isNotFound());
    }
}
