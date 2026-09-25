package com.example.onionarchitecture.inventory.infrastructure.persistence;

import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.domain.repository.StockRepository;
import com.example.onionarchitecture.inventory.infrastructure.persistence.mapper.StockPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class StockRepositoryImpl implements StockRepository {

    private final SpringDataStockRepository repository;
    private final StockPersistenceMapper mapper;

    @Override
    public Optional<StockItem> findByProductId(ProductId productId) {
        return repository.findById(productId.value()).map(mapper::toDomain);
    }

    @Override
    public StockItem save(StockItem stockItem) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(stockItem)));
    }
}
