package com.ananyapraneet.monitoring.orderservice.dto;

import com.ananyapraneet.monitoring.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long userId,
        String product,
        Integer quantity,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
