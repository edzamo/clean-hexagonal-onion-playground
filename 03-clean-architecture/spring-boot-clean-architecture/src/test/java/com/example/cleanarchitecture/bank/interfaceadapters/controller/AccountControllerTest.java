package com.example.cleanarchitecture.bank.interfaceadapters.controller;

import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.AccountNotFoundException;
import com.example.cleanarchitecture.bank.frameworks.web.GlobalExceptionHandler;
import com.example.cleanarchitecture.bank.interfaceadapters.presenter.AccountBalancePresenter;
import com.example.cleanarchitecture.bank.usecases.port.in.DepositUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.GetAccountBalanceUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.OpenAccountUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.WithdrawUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code @WebMvcTest} isn't available yet as a separate artifact in this
 * Spring Boot 4.1.1 release (unlike {@code @WebFluxTest}, which already has
 * its own module) — so this uses {@code MockMvc} in standalone mode instead,
 * wiring the controller directly with mocked use cases, no Spring context.
 */
class AccountControllerTest {

    @Mock
    private OpenAccountUseCase openAccountUseCase;
    @Mock
    private DepositUseCase depositUseCase;
    @Mock
    private WithdrawUseCase withdrawUseCase;
    @Mock
    private TransferUseCase transferUseCase;
    @Mock
    private GetAccountBalanceUseCase getAccountBalanceUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AccountBalancePresenter presenter = new AccountBalancePresenter();
        AccountController controller = new AccountController(
                openAccountUseCase, depositUseCase, withdrawUseCase, transferUseCase, getAccountBalanceUseCase, presenter);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    void openWithBlankHolderNameIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"holderName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBalanceReturns404WhenAccountDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AccountNotFoundException(new AccountId(id))).when(getAccountBalanceUseCase).getBalance(new AccountId(id));

        mockMvc.perform(get("/accounts/{id}/balance", id))
                .andExpect(status().isNotFound());
    }
}
