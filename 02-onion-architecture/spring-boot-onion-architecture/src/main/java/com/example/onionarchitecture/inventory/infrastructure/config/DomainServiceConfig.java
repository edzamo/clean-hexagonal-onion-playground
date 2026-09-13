package com.example.onionarchitecture.inventory.infrastructure.config;

import com.example.onionarchitecture.inventory.domain.service.InventoryDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link InventoryDomainService} deliberately has zero Spring imports — the
 * domain layer in Onion Architecture must stay 100% framework-free, unlike
 * the Interactors/Services in the Clean/Hexagonal projects, which are
 * annotated {@code @Service} directly. Wiring it as a bean lives here, in
 * the outermost layer, instead.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public InventoryDomainService inventoryDomainService() {
        return new InventoryDomainService();
    }
}
