package com.ananyapraneet.monitoring.orderservice.repository;

import com.ananyapraneet.monitoring.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
