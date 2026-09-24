package com.ezamora.coffeeshop.infrastructure.adapter.in.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.ezamora.coffeeshop.application.in.OrderingCoffee;
import com.ezamora.coffeeshop.application.in.PreparingCoffee;
import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.application.out.PaymentNotFound;
import com.ezamora.coffeeshop.domain.model.exception.InvalidCardException;
import com.ezamora.coffeeshop.domain.model.exception.InvalidOrderException;
import com.ezamora.coffeeshop.domain.model.exception.OrderStateException;
import com.ezamora.coffeeshop.infrastructure.error.PersistenceDataCorruptedException;

/** Un test por mapeo excepción de dominio -> ProblemDetail. */
@WebMvcTest(OrderController.class)
class ApiExceptionHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000009");

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private OrderingCoffee orderingCoffee;
    @MockitoBean
    private PreparingCoffee preparingCoffee;

    private ResultActions whenFindingOrderThrows(RuntimeException error) throws Exception {
        when(orderingCoffee.findOrderById(ID)).thenThrow(error);
        return mockMvc.perform(get("/order/{id}", ID));
    }

    private static ResultActions assertProblem(ResultActions result, int expectedStatus) throws Exception {
        return result.andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(expectedStatus));
    }

    @Test
    void orderNotFoundIs404() throws Exception {
        assertProblem(whenFindingOrderThrows(new OrderNotFound("no order")), 404);
    }

    @Test
    void paymentNotFoundIs404() throws Exception {
        assertProblem(whenFindingOrderThrows(new PaymentNotFound("no payment")), 404);
    }

    @Test
    void orderStateExceptionIs409() throws Exception {
        assertProblem(whenFindingOrderThrows(new OrderStateException("wrong state")), 409)
                .andExpect(jsonPath("$.detail").value("wrong state"));
    }

    @Test
    void invalidOrderExceptionIs422() throws Exception {
        assertProblem(whenFindingOrderThrows(new InvalidOrderException("bad order")), 422);
    }

    @Test
    void invalidCardExceptionIs422() throws Exception {
        assertProblem(whenFindingOrderThrows(new InvalidCardException("bad card")), 422);
    }

    @Test
    void optimisticLockFailureIs409WithGenericDetail() throws Exception {
        assertProblem(whenFindingOrderThrows(new ObjectOptimisticLockingFailureException("orders", ID)), 409)
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("orders"))));
    }

    @Test
    void dataIntegrityViolationIs409WithGenericDetail() throws Exception {
        assertProblem(whenFindingOrderThrows(new DataIntegrityViolationException("Duplicate entry uk_payments")), 409)
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("uk_payments"))));
    }

    @Test
    void unexpectedErrorIs500AndDoesNotLeakDetails() throws Exception {
        assertProblem(whenFindingOrderThrows(new IllegalStateException("secret jdbc url")), 500)
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
    }

    @Test
    void corruptedPersistenceDataIs500NotAClientError() throws Exception {
        assertProblem(whenFindingOrderThrows(new PersistenceDataCorruptedException("row 7 has no items", null)), 500)
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("row 7"))));
    }

    @Test
    void malformedIdIs400() throws Exception {
        assertProblem(mockMvc.perform(get("/order/not-a-uuid")), 400);
    }
}
