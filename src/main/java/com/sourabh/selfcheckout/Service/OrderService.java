package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.CartItemResponse;
import com.sourabh.selfcheckout.Dto.OrderHistoryResponse;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Enum.CartStatus;
import com.sourabh.selfcheckout.Repo.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final CartRepository cartRepo;

    public OrderService(CartRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    public List<OrderHistoryResponse> getOrderHistory(String userId) {
        // ✅ Get all CHECKED_OUT carts for this user, newest first
        return cartRepo
                .findByUserIdAndStatusOrderByCreatedAtDesc(
                        userId, CartStatus.CHECKED_OUT)
                .stream()
                .map(this::toOrderHistoryResponse)
                .toList();
    }

    private OrderHistoryResponse toOrderHistoryResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getBarcode(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity()
                ))
                .toList();

        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();

        return new OrderHistoryResponse(
                cart.getId(),
                cart.getCreatedAt(),
                items,
                total,
                cart.getStatus().name()
        );
    }
}