package com.ananyapraneet.monitoring.orderservice.controller;

import com.ananyapraneet.monitoring.orderservice.dto.OrderResponse;
import com.ananyapraneet.monitoring.orderservice.entity.OrderStatus;
import com.ananyapraneet.monitoring.orderservice.exception.GlobalExceptionHandler;
import com.ananyapraneet.monitoring.orderservice.exception.OrderNotFoundException;
import com.ananyapraneet.monitoring.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_shouldReturn201() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "Mechanical Keyboard",
                2,
                new BigDecimal("259.98"),
                OrderStatus.CREATED,
                null,
                null
        );

        when(orderService.createOrder(any())).thenReturn(response);

        String request = """
                {
                    "userId": 1,
                    "product": "Mechanical Keyboard",
                    "quantity": 2,
                    "amount": 259.98
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.product").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrder_shouldReturn200() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "MacBook Pro",
                1,
                new BigDecimal("1499.99"),
                OrderStatus.CREATED,
                null,
                null
        );

        when(orderService.getOrder(1L)).thenReturn(response);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.product").value("MacBook Pro"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrder_shouldReturn404WhenOrderDoesNotExist() throws Exception {
        when(orderService.getOrder(99L))
                .thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Order not found with id: 99"));
    }

    @Test
    void getAllOrders_shouldReturn200() throws Exception {
        List<OrderResponse> orders = List.of(
                new OrderResponse(
                        1L, 1L, "Keyboard", 1,
                        new BigDecimal("129.99"),
                        OrderStatus.CREATED, null, null
                ),
                new OrderResponse(
                        2L, 2L, "Mouse", 2,
                        new BigDecimal("59.98"),
                        OrderStatus.PROCESSING, null, null
                )
        );

        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].product").value("Keyboard"))
                .andExpect(jsonPath("$[1].product").value("Mouse"));
    }

    @Test
    void updateOrder_shouldReturn200() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "Mechanical Keyboard",
                2,
                new BigDecimal("259.98"),
                OrderStatus.PROCESSING,
                null,
                null
        );

        when(orderService.updateOrder(eq(1L), any())).thenReturn(response);

        String request = """
                {
                    "userId": 1,
                    "product": "Mechanical Keyboard",
                    "quantity": 2,
                    "amount": 259.98,
                    "status": "PROCESSING"
                }
                """;

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void deleteOrder_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createOrder_shouldReturn400ForInvalidRequest() throws Exception {
        String request = """
                {
                    "userId": null,
                    "product": "",
                    "quantity": 0,
                    "amount": 0
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.errors.userId")
                        .value("userId is required"))
                .andExpect(jsonPath("$.errors.product")
                        .value("product is required"))
                .andExpect(jsonPath("$.errors.quantity")
                        .value("quantity must be at least 1"))
                .andExpect(jsonPath("$.errors.amount")
                        .value("amount must be greater than 0"));
    }
}
