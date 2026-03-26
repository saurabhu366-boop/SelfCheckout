package com.sourabh.selfcheckout.Controller;

import com.sourabh.selfcheckout.Dto.OrderHistoryResponse;
import com.sourabh.selfcheckout.Service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/history")
    public List<OrderHistoryResponse> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return orderService.getOrderHistory(userDetails.getUsername());
    }
}