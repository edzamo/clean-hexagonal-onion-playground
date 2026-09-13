package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.in.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
public class OrderController {

  @GetMapping
  public Mono<String> getOrders() {
    return Mono.just("List of orders");
  }


}
