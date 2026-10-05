package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.OrderHistoryItemResponse;
import com.sourabh.selfcheckout.Dto.OrderHistoryResponse;
import com.sourabh.selfcheckout.Entity.Order;
import com.sourabh.selfcheckout.Entity.OrderItem;
import com.sourabh.selfcheckout.Repo.CustomerRepository;
import com.sourabh.selfcheckout.Repo.OrderItemRepository;
import com.sourabh.selfcheckout.Repo.OrderRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CustomerRepository customerRepo;

    public OrderService(OrderRepository orderRepo,
                        OrderItemRepository orderItemRepo,
                        CustomerRepository customerRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.customerRepo = customerRepo;
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getOrderHistory(String email) {
        var customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Customer not found for email: " + email));

        List<Order> orders = orderRepo.findByCustomer_CustomerIdOrderByCreatedAtDescOrderIdDesc(
                customer.getCustomerId());
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getOrderId).toList();
        Map<Long, List<OrderItem>> itemsByOrderId = orderItemRepo
                .findWithProductsByOrderIds(orderIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getOrderId()));

        return orders.stream()
                .map(order -> toOrderHistoryResponse(
                        order,
                        itemsByOrderId.getOrDefault(order.getOrderId(), List.of())))
                .toList();
    }

    private OrderHistoryResponse toOrderHistoryResponse(Order order, List<OrderItem> orderItems) {
        List<OrderHistoryItemResponse> items = orderItems.stream()
                .map(orderItem -> new OrderHistoryItemResponse(
                        orderItem.getProduct().getBarcode(),
                        orderItem.getProduct().getName(),
                        orderItem.getUnitPrice().doubleValue(),
                        orderItem.getQuantity(),
                        orderItem.getSubtotal()
                ))
                .toList();

        return new OrderHistoryResponse(
                order.getOrderId(),
                order.getCreatedAt(),
                items,
                order.getTotalAmount().doubleValue(),
                order.getStatus()
        );
    }
}
