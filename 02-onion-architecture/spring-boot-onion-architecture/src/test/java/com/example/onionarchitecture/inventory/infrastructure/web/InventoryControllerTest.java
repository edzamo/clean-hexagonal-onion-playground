package com.example.onionarchitecture.inventory.infrastructure.web;

import com.example.onionarchitecture.inventory.application.service.DiscontinueProductService;
import com.example.onionarchitecture.inventory.application.service.GetStockLevelService;
import com.example.onionarchitecture.inventory.application.service.ReceiveStockService;
import com.example.onionarchitecture.inventory.application.service.RegisterProductService;
import com.example.onionarchitecture.inventory.application.service.ReserveStockService;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code @WebMvcTest} isn't available yet as a separate artifact in this
 * Spring Boot 4.1.1 release — same finding as in the Clean Architecture
 * project. Standalone {@code MockMvc} instead.
 */
class InventoryControllerTest {

    @Mock
    private RegisterProductService registerProductService;
    @Mock
    private ReceiveStockService receiveStockService;
    @Mock
    private ReserveStockService reserveStockService;
    @Mock
    private DiscontinueProductService discontinueProductService;
    @Mock
    private GetStockLevelService getStockLevelService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        InventoryController controller = new InventoryController(
                registerProductService, receiveStockService, reserveStockService,
                discontinueProductService, getStockLevelService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    void registerWithBlankSkuIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"\",\"name\":\"Coffee\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStockReturns404WhenProductDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(getStockLevelService.getStockLevel(new ProductId(id)))
                .thenThrow(new ProductNotFoundException(new ProductId(id)));

        mockMvc.perform(get("/products/{id}/stock", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void receiveWithZeroQuantityIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post("/products/{id}/stock/receive", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }
}
