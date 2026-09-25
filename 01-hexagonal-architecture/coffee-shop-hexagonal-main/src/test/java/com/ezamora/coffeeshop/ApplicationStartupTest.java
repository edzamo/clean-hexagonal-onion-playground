package com.ezamora.coffeeshop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.ezamora.coffeeshop.application.in.OrderingCoffee;
import com.ezamora.coffeeshop.application.in.PreparingCoffee;
import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.application.out.Payments;
import com.ezamora.coffeeshop.application.service.CoffeeMachine;
import com.ezamora.coffeeshop.application.service.CoffeeShop;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.OrderServiceAdapter;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment.PaymentServiceAdapter;
import com.ezamora.coffeeshop.infrastructure.config.DatabaseInitializer;

/** Test de arranque: el contexto completo (H2, perfil test) cablea puertos, casos de uso y adaptadores. */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationStartupTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void useCasesArePublishedThroughTheirInputPorts() {
        assertThat(context.getBean(OrderingCoffee.class)).isInstanceOf(CoffeeShop.class);
        assertThat(context.getBean(PreparingCoffee.class)).isInstanceOf(CoffeeMachine.class);
    }

    @Test
    void orderingCoffeeIsTransactionalProxyBecauseOfPayOrder() {
        assertThat(AopUtils.isAopProxy(context.getBean(OrderingCoffee.class))).isTrue();
    }

    @Test
    void preparingCoffeeIsAPlainBean() {
        assertThat(AopUtils.isAopProxy(context.getBean(PreparingCoffee.class))).isFalse();
    }

    @Test
    void outputPortsAreBoundToThePersistenceAdapters() {
        assertThat(context.getBean(Orders.class)).isInstanceOf(OrderServiceAdapter.class);
        assertThat(context.getBean(Payments.class)).isInstanceOf(PaymentServiceAdapter.class);
    }

    @Test
    void inputAdapterExposesThePaymentRoute() {
        var patterns = context.getBean(RequestMappingHandlerMapping.class).getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPathPatternsCondition().getPatternValues().stream()).toList();

        assertThat(patterns).contains("/order/{id}/pay");
    }

    @Test
    void mysqlSeedInitializerIsDisabledInTheTestProfile() {
        assertThat(context.getBeansOfType(DatabaseInitializer.class)).isEmpty();
    }
}
