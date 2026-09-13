package com.example.onionarchitecture.inventory.infrastructure.web;

import com.example.onionarchitecture.inventory.application.dto.ReceiveStockCommand;
import com.example.onionarchitecture.inventory.application.dto.ReserveStockCommand;
import com.example.onionarchitecture.inventory.application.service.DiscontinueProductService;
import com.example.onionarchitecture.inventory.application.service.GetStockLevelService;
import com.example.onionarchitecture.inventory.application.service.ReceiveStockService;
import com.example.onionarchitecture.inventory.application.service.RegisterProductService;
import com.example.onionarchitecture.inventory.application.service.ReserveStockService;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class InventoryController {

    private final RegisterProductService registerProductService;
    private final ReceiveStockService receiveStockService;
    private final ReserveStockService reserveStockService;
    private final DiscontinueProductService discontinueProductService;
    private final GetStockLevelService getStockLevelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse register(@Valid @RequestBody RegisterProductRequest request) {
        return ProductResponse.from(registerProductService.register(request.toCommand()));
    }

    @GetMapping("/{id}/stock")
    public StockLevelResponse getStockLevel(@PathVariable UUID id) {
        return StockLevelResponse.from(getStockLevelService.getStockLevel(new ProductId(id)));
    }

    @PostMapping("/{id}/stock/receive")
    public StockLevelResponse receive(@PathVariable UUID id, @Valid @RequestBody QuantityRequest request) {
        receiveStockService.receive(new ReceiveStockCommand(new ProductId(id), request.quantity()));
        return StockLevelResponse.from(getStockLevelService.getStockLevel(new ProductId(id)));
    }

    @PostMapping("/{id}/stock/reserve")
    public StockLevelResponse reserve(@PathVariable UUID id, @Valid @RequestBody QuantityRequest request) {
        reserveStockService.reserve(new ReserveStockCommand(new ProductId(id), request.quantity()));
        return StockLevelResponse.from(getStockLevelService.getStockLevel(new ProductId(id)));
    }

    @PostMapping("/{id}/discontinue")
    public ProductResponse discontinue(@PathVariable UUID id) {
        return ProductResponse.from(discontinueProductService.discontinue(new ProductId(id)));
    }
}
