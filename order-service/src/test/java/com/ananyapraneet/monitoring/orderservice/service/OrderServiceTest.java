package com.ananyapraneet.monitoring.orderservice.service;

import com.ananyapraneet.monitoring.orderservice.dto.CreateOrderRequest;
import com.ananyapraneet.monitoring.orderservice.dto.OrderResponse;
import com.ananyapraneet.monitoring.orderservice.dto.UpdateOrderRequest;
import com.ananyapraneet.monitoring.orderservice.entity.Order;
import com.ananyapraneet.monitoring.orderservice.entity.OrderStatus;
import com.ananyapraneet.monitoring.orderservice.exception.OrderNotFoundException;
import com.ananyapraneet.monitoring.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.MeterRegistry;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        orderService = new OrderService(orderRepository, meterRegistry);
    }

    @Test
    void createOrder_shouldCreateOrderWithCreatedStatus() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                "Mechanical Keyboard",
                2,
                new BigDecimal("259.98")
        );

        Order savedOrder = new Order();
        savedOrder.setUserId(1L);
        savedOrder.setProduct("Mechanical Keyboard");
        savedOrder.setQuantity(2);
        savedOrder.setAmount(new BigDecimal("259.98"));
        savedOrder.setStatus(OrderStatus.CREATED);

        when(orderRepository.saveAndFlush(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertEquals(1L, response.userId());
        assertEquals("Mechanical Keyboard", response.product());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("259.98"), response.amount());
        assertEquals(OrderStatus.CREATED, response.status());

        verify(orderRepository).saveAndFlush(any(Order.class));
    }

    @Test
    void createOrder_shouldIncrementFailedCounterWhenSaveAndFlushFails() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                "Failure Test Product",
                1,
                new BigDecimal("99.99")
        );

        RuntimeException exception = new RuntimeException("Database failure");

        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenThrow(exception);

        assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(request)
        );

        assertThat(
                meterRegistry.get("orders_failed_total")
                        .counter()
                        .count()
        ).isEqualTo(1.0);
    }

    @Test
    void getOrder_shouldReturnOrderWhenOrderExists() {
        Order order = new Order();
        order.setUserId(1L);
        order.setProduct("MacBook Pro");
        order.setQuantity(1);
        order.setAmount(new BigDecimal("1499.99"));
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(1L);

        assertEquals(1L, response.userId());
        assertEquals("MacBook Pro", response.product());
        assertEquals(OrderStatus.CREATED, response.status());

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrder_shouldThrowExceptionWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrder(99L)
        );

        verify(orderRepository).findById(99L);
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        Order firstOrder = new Order();
        firstOrder.setUserId(1L);
        firstOrder.setProduct("Keyboard");
        firstOrder.setQuantity(1);
        firstOrder.setAmount(new BigDecimal("129.99"));
        firstOrder.setStatus(OrderStatus.CREATED);

        Order secondOrder = new Order();
        secondOrder.setUserId(2L);
        secondOrder.setProduct("Mouse");
        secondOrder.setQuantity(2);
        secondOrder.setAmount(new BigDecimal("59.98"));
        secondOrder.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findAll()).thenReturn(List.of(firstOrder, secondOrder));

        List<OrderResponse> response = orderService.getAllOrders();

        assertEquals(2, response.size());
        assertEquals("Keyboard", response.get(0).product());
        assertEquals("Mouse", response.get(1).product());

        verify(orderRepository).findAll();
    }

    @Test
    void updateOrder_shouldUpdateOrderDetails() {
        Order existingOrder = new Order();
        existingOrder.setUserId(1L);
        existingOrder.setProduct("Keyboard");
        existingOrder.setQuantity(1);
        existingOrder.setAmount(new BigDecimal("129.99"));
        existingOrder.setStatus(OrderStatus.CREATED);

        UpdateOrderRequest request = new UpdateOrderRequest(
                1L,
                "Mechanical Keyboard",
                2,
                new BigDecimal("259.98"),
                OrderStatus.PROCESSING
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.saveAndFlush(existingOrder)).thenReturn(existingOrder);

        OrderResponse response = orderService.updateOrder(1L, request);

        assertEquals("Mechanical Keyboard", response.product());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("259.98"), response.amount());
        assertEquals(OrderStatus.PROCESSING, response.status());

        verify(orderRepository).findById(1L);
        verify(orderRepository).saveAndFlush(existingOrder);
    }

    @Test
    void deleteOrder_shouldDeleteExistingOrder() {
        Order order = new Order();
        order.setUserId(1L);
        order.setProduct("Keyboard");
        order.setQuantity(1);
        order.setAmount(new BigDecimal("129.99"));
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).findById(1L);
        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_shouldThrowExceptionWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.deleteOrder(99L)
        );

        verify(orderRepository, never()).delete(any(Order.class));
    }
}
