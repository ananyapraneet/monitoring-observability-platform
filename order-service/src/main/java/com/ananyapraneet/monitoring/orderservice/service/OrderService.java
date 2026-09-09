package com.ananyapraneet.monitoring.orderservice.service;

import com.ananyapraneet.monitoring.orderservice.dto.CreateOrderRequest;
import com.ananyapraneet.monitoring.orderservice.dto.OrderResponse;
import com.ananyapraneet.monitoring.orderservice.dto.UpdateOrderRequest;
import com.ananyapraneet.monitoring.orderservice.entity.Order;
import com.ananyapraneet.monitoring.orderservice.entity.OrderStatus;
import com.ananyapraneet.monitoring.orderservice.exception.OrderNotFoundException;
import com.ananyapraneet.monitoring.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.userId());
        order.setProduct(request.product());
        order.setQuantity(request.quantity());
        order.setAmount(request.amount());
        order.setStatus(OrderStatus.CREATED);

        Order savedOrder = orderRepository.save(order);

        log.info("Created order with id={} for userId={}",
                savedOrder.getId(), savedOrder.getUserId());

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        Order order = findOrder(id);

        order.setUserId(request.userId());
        order.setProduct(request.product());
        order.setQuantity(request.quantity());
        order.setAmount(request.amount());
        order.setStatus(request.status());

        Order updatedOrder = orderRepository.saveAndFlush(order);

        log.info("Updated order with id={} to status={}",
                updatedOrder.getId(), updatedOrder.getStatus());

        return toResponse(updatedOrder);
    }

    public void deleteOrder(Long id) {
        Order order = findOrder(id);

        orderRepository.delete(order);

        log.info("Deleted order with id={}", id);
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProduct(),
                order.getQuantity(),
                order.getAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
