package com.ezamora.coffeeshop.infrastructure.adapter.in.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.application.out.PaymentNotFound;
import com.ezamora.coffeeshop.domain.model.exception.InvalidCardException;
import com.ezamora.coffeeshop.domain.model.exception.InvalidOrderException;
import com.ezamora.coffeeshop.domain.model.exception.OrderStateException;

/** Traducción centralizada de errores de dominio/aplicación a {@link ProblemDetail}. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({ OrderNotFound.class, PaymentNotFound.class })
    ProblemDetail notFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OrderStateException.class)
    ProblemDetail conflict(OrderStateException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({ InvalidOrderException.class, InvalidCardException.class })
    ProblemDetail unprocessable(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** Concurrencia: conflicto de versión o de unicidad (p. ej. doble pago). Detalle genérico, sin SQL. */
    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class })
    ProblemDetail concurrencyConflict(Exception ex) {
        LOG.warn("Concurrency conflict: {}", ex.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The request conflicts with the current state of the resource; retry after reloading it");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        LOG.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error");
    }

    /** Validación de DTOs (Bean Validation): 422 con los campos inválidos, sin valores. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage()).sorted().toList();
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,
                "Invalid request: " + String.join("; ", fields));
        return ResponseEntity.unprocessableEntity().body(problem);
    }
}
