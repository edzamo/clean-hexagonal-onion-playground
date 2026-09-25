package com.example.onionarchitecture.inventory.infrastructure.persistence.mapper;

import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.infrastructure.persistence.entity.StockJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class StockPersistenceMapper {

    public StockJpaEntity toJpaEntity(StockItem stockItem) {
        return new StockJpaEntity(stockItem.getProductId().value(), stockItem.getQuantityOnHand());
    }

    public StockItem toDomain(StockJpaEntity entity) {
        return new StockItem(new ProductId(entity.getProductId()), entity.getQuantityOnHand());
    }
}
