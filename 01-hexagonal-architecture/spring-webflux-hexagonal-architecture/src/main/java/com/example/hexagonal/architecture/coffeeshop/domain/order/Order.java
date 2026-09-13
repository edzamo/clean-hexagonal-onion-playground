package com.example.hexagonal.architecture.coffeeshop.domain.order;

import java.util.List;
import java.util.UUID;

public record Order( UUID id,
Location location,
List<LineItem> items,
 Status status  ) {





}
