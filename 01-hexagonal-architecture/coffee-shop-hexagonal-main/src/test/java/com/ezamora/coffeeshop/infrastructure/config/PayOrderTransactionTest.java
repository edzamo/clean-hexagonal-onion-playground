package com.ezamora.coffeeshop.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.ezamora.coffeeshop.application.in.OrderingCoffee;
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
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.OrderRepository;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.OrderServiceAdapter;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment.PaymentRepository;

/** Límite transaccional de payOrder con beans reales y H2. */
@SpringBootTest
@ActiveProfiles("test")
class PayOrderTransactionTest {

    private static final String PAN = "4111111111111111";

    @Autowired
    private OrderingCoffee orderingCoffee;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockitoSpyBean
    private OrderServiceAdapter ordersAdapter;

    @AfterEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
    }

    private Order placedOrder() {
        return orderingCoffee.placeOrder(Order.create(Location.TAKE_AWAY,
                List.of(new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1))));
    }

    private static CreditCard card() {
        return new CreditCard("Ana Perez", PAN, Month.DECEMBER, Year.now().plusYears(3));
    }

    @Test
    void ifSavingThePaidOrderFailsNoPaymentRemainsPersisted() {
        var order = placedOrder();
        doThrow(new IllegalStateException("boom")).when(ordersAdapter)
                .save(argThat(o -> o.getStatus() == Status.PAID));

        assertThatThrownBy(() -> orderingCoffee.payOrder(order.getId(), card()))
                .isInstanceOf(IllegalStateException.class).hasMessage("boom");

        assertThat(paymentRepository.count()).isZero();
        assertThat(orderingCoffee.findOrderById(order.getId()).getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
    }

    @Test
    void savingTheOrderAfterPaymentNeitherDeletesNorRecreatesThePayment() {
        var order = placedOrder();
        orderingCoffee.payOrder(order.getId(), card());
        var paymentIdBefore = paymentRepository.findAll().get(0).getId();

        ordersAdapter.save(orderingCoffee.findOrderById(order.getId()).startPreparing());

        assertThat(paymentRepository.findAll()).hasSize(1);
        assertThat(paymentRepository.findAll().get(0).getId()).isEqualTo(paymentIdBefore);
    }

    @Test
    void twoConcurrentPaymentsOfTheSameOrderProduceExactlyOnePayment() throws Exception {
        var order = placedOrder();
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Object>> results = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    try {
                        return orderingCoffee.payOrder(order.getId(), card());
                    } catch (RuntimeException e) {
                        return e;
                    }
                }));
            }
            start.countDown();
            var outcomes = new ArrayList<Object>();
            for (var f : results) {
                outcomes.add(f.get(20, TimeUnit.SECONDS));
            }

            assertThat(outcomes.stream().filter(o -> o instanceof Payment)).hasSize(1);
            assertThat(outcomes.stream().filter(o -> o instanceof RuntimeException)).allMatch(e ->
                    e instanceof OrderStateException || e instanceof DataIntegrityViolationException
                            || e instanceof ObjectOptimisticLockingFailureException);
            assertThat(paymentRepository.count()).isEqualTo(1);
            assertThat(orderingCoffee.findOrderById(order.getId()).getStatus()).isEqualTo(Status.PAID);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void successfulPaymentIsPersistedAndNoColumnContainsTheFullCardNumber() {
        var order = placedOrder();

        orderingCoffee.payOrder(order.getId(), card());

        assertThat(paymentRepository.count()).isEqualTo(1);
        var allValues = jdbcTemplate.queryForList("SELECT * FROM payments").stream()
                .flatMap(row -> row.values().stream()).map(String::valueOf).collect(Collectors.toList());
        assertThat(allValues).noneMatch(value -> value.contains(PAN));
        assertThat(allValues).contains("1111");
    }
}
