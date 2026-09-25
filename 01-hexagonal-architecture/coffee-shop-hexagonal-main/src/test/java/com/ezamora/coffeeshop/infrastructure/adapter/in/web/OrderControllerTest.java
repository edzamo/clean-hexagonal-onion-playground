package com.ezamora.coffeeshop.infrastructure.adapter.in.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ezamora.coffeeshop.application.in.OrderingCoffee;
import com.ezamora.coffeeshop.application.in.PreparingCoffee;
import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.domain.model.enums.Drink;
import com.ezamora.coffeeshop.domain.model.enums.Location;
import com.ezamora.coffeeshop.domain.model.enums.Milk;
import com.ezamora.coffeeshop.domain.model.enums.Size;
import com.ezamora.coffeeshop.domain.model.enums.Status;
import com.ezamora.coffeeshop.domain.model.exception.OrderStateException;
import com.ezamora.coffeeshop.domain.model.order.LineItem;
import com.ezamora.coffeeshop.domain.model.order.Order;
import com.ezamora.coffeeshop.domain.model.payment.CreditCard;
import com.ezamora.coffeeshop.domain.model.payment.Payment;
import com.ezamora.coffeeshop.domain.model.payment.Receipt;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.LineItemRequest;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.OrderRequest;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.PayRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000009");
    private static final String PAN = "4111111111111111";
    private static final LineItem LATTE = new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderingCoffee orderingCoffee;
    @MockitoBean
    private PreparingCoffee preparingCoffee;

    private static Order orderIn(Status status) {
        return Order.rehydrate(ID, Location.TAKE_AWAY, List.of(LATTE), status);
    }

    @Test
    void createOrderReturnsCreatedWithLocationIdAndStatus() throws Exception {
        var request = new OrderRequest(Location.TAKE_AWAY, List.of(new LineItemRequest(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1)));
        when(orderingCoffee.placeOrder(any(Order.class))).thenReturn(orderIn(Status.PAYMENT_EXPECTED));

        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/order/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.status").value("PAYMENT_EXPECTED"))
                .andExpect(jsonPath("$.location").value("TAKE_AWAY"))
                .andExpect(jsonPath("$.items[0].drink").value("LATTE"));
        verify(orderingCoffee).placeOrder(any(Order.class));
    }

    @Test
    void createOrderWithNullQuantityIsRejectedWith422InsteadOfAnNpe() throws Exception {
        var body = "{\"location\":\"TAKE_AWAY\",\"items\":[{\"drink\":\"LATTE\",\"milk\":\"WHOLE\",\"size\":\"LARGE\"}]}";

        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void getOrderReturnsTheOrder() throws Exception {
        when(orderingCoffee.findOrderById(ID)).thenReturn(orderIn(Status.PAID));

        mockMvc.perform(get("/order/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void payOrderReturnsThePaymentWithoutTheCardNumber() throws Exception {
        when(orderingCoffee.payOrder(eq(ID), any(CreditCard.class)))
                .thenReturn(new Payment(ID, "1111", "Ana Perez", new BigDecimal("5.0"), LocalDate.of(2025, 6, 15)));
        var request = new PayRequest("Ana Perez", PAN, 12, 2035);

        var result = mockMvc.perform(post("/order/{id}/pay", ID).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(ID.toString()))
                .andExpect(jsonPath("$.last4").value("1111"))
                .andExpect(jsonPath("$.paid").value("2025-06-15"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain(PAN);
    }

    @Test
    void payOrderWithAnInvalidMonthIsRejectedWith422() throws Exception {
        var body = objectMapper.writeValueAsString(new PayRequest("Ana Perez", PAN, 13, 2035));

        mockMvc.perform(post("/order/{id}/pay", ID).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    private void assertPayRejected(PayRequest request) throws Exception {
        mockMvc.perform(post("/order/{id}/pay", ID).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void payOrderWithAHugeExpiryYearIs422Not500() throws Exception {
        assertPayRejected(new PayRequest("Ana Perez", PAN, 12, 999999999));
    }

    @Test
    void payOrderWithA300CharHolderIs422() throws Exception {
        assertPayRejected(new PayRequest("A".repeat(300), PAN, 12, 2035));
    }

    @Test
    void payOrderWithNonNumericCardNumberIs422() throws Exception {
        assertPayRejected(new PayRequest("Ana Perez", "4111-abcd-1111", 12, 2035));
    }

    @Test
    void createOrderWithHugeQuantityIs422() throws Exception {
        var body = "{\"location\":\"TAKE_AWAY\",\"items\":[{\"drink\":\"LATTE\",\"milk\":\"WHOLE\",\"size\":\"LARGE\",\"quantity\":2147483647}]}";

        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createOrderWithZeroQuantityIs422() throws Exception {
        var body = "{\"location\":\"TAKE_AWAY\",\"items\":[{\"drink\":\"LATTE\",\"milk\":\"WHOLE\",\"size\":\"LARGE\",\"quantity\":0}]}";

        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createOrderWithMoreThan50ItemsIs422() throws Exception {
        var item = new LineItemRequest(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1);
        var request = new OrderRequest(Location.TAKE_AWAY, java.util.Collections.nCopies(51, item));

        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void payRequestToStringMasksTheCardNumber() {
        assertThat(new PayRequest("Ana Perez", PAN, 12, 2035).toString()).doesNotContain(PAN).contains("1111");
    }

    @Test
    void readReceiptReturnsAmountAndPaymentDate() throws Exception {
        when(orderingCoffee.readReceipt(ID)).thenReturn(new Receipt(new BigDecimal("5.0"), LocalDate.of(2025, 6, 15)));

        mockMvc.perform(get("/order/{id}/receipt", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5.0))
                .andExpect(jsonPath("$.paid").value("2025-06-15"));
    }

    @Test
    void startPreparingReturnsTheOrderInPreparing() throws Exception {
        when(preparingCoffee.startPreparingOrder(ID)).thenReturn(orderIn(Status.PREPARING));

        mockMvc.perform(put("/order/{id}/prepare/start", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    void finishPreparingReturnsTheOrderReady() throws Exception {
        when(preparingCoffee.finishPreparingOrder(ID)).thenReturn(orderIn(Status.READY));

        mockMvc.perform(put("/order/{id}/prepare/finish", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void takeOrderReturnsTheOrderTaken() throws Exception {
        when(orderingCoffee.takeOrder(ID)).thenReturn(orderIn(Status.TAKEN));

        mockMvc.perform(put("/order/{id}/take", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TAKEN"));
    }

    @Test
    void updateOrderReturnsTheUpdatedOrder() throws Exception {
        var request = new OrderRequest(Location.IN_STORE, List.of(new LineItemRequest(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2)));
        when(orderingCoffee.updateOrder(eq(ID), any(Order.class))).thenReturn(orderIn(Status.PAYMENT_EXPECTED));

        mockMvc.perform(post("/order/{id}", ID).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void updatingAPaidOrderIs409() throws Exception {
        var request = new OrderRequest(Location.IN_STORE, List.of(new LineItemRequest(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2)));
        when(orderingCoffee.updateOrder(eq(ID), any(Order.class))).thenThrow(new OrderStateException("paid"));

        mockMvc.perform(post("/order/{id}", ID).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelOrderReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/order/{id}", ID)).andExpect(status().isNoContent());

        verify(orderingCoffee).cancelOrder(ID);
    }

    @Test
    void cancellingAPaidOrderIs409() throws Exception {
        doThrow(new OrderStateException("paid")).when(orderingCoffee).cancelOrder(ID);

        mockMvc.perform(delete("/order/{id}", ID)).andExpect(status().isConflict());
    }

    @Test
    void startPreparingUnknownOrderIs404() throws Exception {
        when(preparingCoffee.startPreparingOrder(ID)).thenThrow(new OrderNotFound("missing"));

        mockMvc.perform(put("/order/{id}/prepare/start", ID)).andExpect(status().isNotFound());
    }

    @Test
    void startPreparingAnUnpaidOrderIs409() throws Exception {
        when(preparingCoffee.startPreparingOrder(ID)).thenThrow(new OrderStateException("not paid"));

        mockMvc.perform(put("/order/{id}/prepare/start", ID)).andExpect(status().isConflict());
    }

    @Test
    void finishPreparingUnknownOrderIs404() throws Exception {
        when(preparingCoffee.finishPreparingOrder(ID)).thenThrow(new OrderNotFound("missing"));

        mockMvc.perform(put("/order/{id}/prepare/finish", ID)).andExpect(status().isNotFound());
    }

    @Test
    void finishPreparingAnOrderNotBeingPreparedIs409() throws Exception {
        when(preparingCoffee.finishPreparingOrder(ID)).thenThrow(new OrderStateException("not preparing"));

        mockMvc.perform(put("/order/{id}/prepare/finish", ID)).andExpect(status().isConflict());
    }

    @Test
    void takeUnknownOrderIs404() throws Exception {
        when(orderingCoffee.takeOrder(ID)).thenThrow(new OrderNotFound("missing"));

        mockMvc.perform(put("/order/{id}/take", ID)).andExpect(status().isNotFound());
    }

    @Test
    void takeAnOrderThatIsNotReadyIs409() throws Exception {
        when(orderingCoffee.takeOrder(ID)).thenThrow(new OrderStateException("not ready"));

        mockMvc.perform(put("/order/{id}/take", ID)).andExpect(status().isConflict());
    }
}
