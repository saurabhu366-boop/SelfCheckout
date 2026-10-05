package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.CheckoutResponse;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Entity.CartItem;
import com.sourabh.selfcheckout.Entity.Customer;
import com.sourabh.selfcheckout.Entity.Inventory;
import com.sourabh.selfcheckout.Entity.Order;
import com.sourabh.selfcheckout.Entity.OrderItem;
import com.sourabh.selfcheckout.Entity.Payment;
import com.sourabh.selfcheckout.Entity.Product;
import com.sourabh.selfcheckout.Enum.CartStatus;
import com.sourabh.selfcheckout.Repo.CartItemRepository;
import com.sourabh.selfcheckout.Repo.CartRepository;
import com.sourabh.selfcheckout.Repo.CustomerRepository;
import com.sourabh.selfcheckout.Repo.InventoryRepository;
import com.sourabh.selfcheckout.Repo.OrderItemRepository;
import com.sourabh.selfcheckout.Repo.OrderRepository;
import com.sourabh.selfcheckout.Repo.PaymentRepository;
import com.sourabh.selfcheckout.Repo.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServicePaymentFailureTest {

    @Test
    void checkoutWhenLocalPaymentIsDeclinedMarksRecordsFailedAndPreservesCartAndInventory() {
        ProductRepository productRepository = mock(ProductRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        CartItemRepository cartItemRepository = mock(CartItemRepository.class);
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentProcessor paymentProcessor = mock(PaymentProcessor.class);

        Customer customer = new Customer();
        customer.setCustomerId(999L);
        customer.setEmail("failure-test@grozo.local");

        Product product = new Product();
        product.setId(2L);
        product.setBarcode("7622202832857");
        product.setName("CADBURY CELEBRATION");
        product.setMrp(new BigDecimal("110.00"));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);

        Cart cart = new Cart();
        cart.setId(999L);
        cart.setCustomer(customer);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setItems(new ArrayList<>());

        CartItem cartItem = new CartItem(1L, cart, product, 2);
        cartItem.setUnitPrice(new BigDecimal("110.00"));
        cart.getItems().add(cartItem);

        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerIdAndStatusForUpdate(999L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(inventoryRepository.findByProductIdForUpdate(2L)).thenReturn(Optional.of(inventory));
        List<OrderItem> savedItems = new ArrayList<>();
        when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<OrderItem> items = invocation.getArgument(0);
            items.forEach(savedItems::add);
            return savedItems;
        });
        List<String> orderStatusesAtSave = new ArrayList<>();
        List<BigDecimal> orderTotalsAtSave = new ArrayList<>();
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            orderStatusesAtSave.add(order.getStatus());
            orderTotalsAtSave.add(order.getTotalAmount());
            return order;
        });
        List<String> paymentStatusesAtSave = new ArrayList<>();
        List<String> paymentMethodsAtSave = new ArrayList<>();
        List<BigDecimal> paymentAmountsAtSave = new ArrayList<>();
        List<String> transactionIdsAtSave = new ArrayList<>();
        List<java.time.LocalDateTime> paidAtValuesAtSave = new ArrayList<>();
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            paymentStatusesAtSave.add(payment.getStatus());
            paymentMethodsAtSave.add(payment.getPaymentMethod());
            paymentAmountsAtSave.add(payment.getAmount());
            transactionIdsAtSave.add(payment.getTransactionId());
            paidAtValuesAtSave.add(payment.getPaidAt());
            return payment;
        });
        when(paymentProcessor.processPayment("UPI", new BigDecimal("220.00"))).thenReturn(false);

        CartService service = new CartService(
                productRepository,
                cartRepository,
                cartItemRepository,
                inventoryRepository,
                customerRepository,
                orderRepository,
                orderItemRepository,
                paymentRepository,
                paymentProcessor);

        CheckoutResponse response = service.checkout(customer.getEmail(), "UPI");

        assertEquals(999L, response.getCartId());
        assertEquals(220.0, response.getTotalAmount());
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().toLowerCase().contains("payment failed"));
        assertFalse("CHECKED_OUT".equals(response.getStatus()));

        verify(orderRepository, org.mockito.Mockito.times(2)).save(any(Order.class));
        assertEquals(List.of("PENDING", "FAILED"), orderStatusesAtSave);
        assertEquals(List.of(new BigDecimal("220.00"), new BigDecimal("220.00")), orderTotalsAtSave);
        assertNotEquals("PAID", orderStatusesAtSave.get(1));

        verify(paymentRepository, org.mockito.Mockito.times(2)).save(any(Payment.class));
        assertEquals(List.of("PENDING", "FAILED"), paymentStatusesAtSave);
        assertNotEquals("SUCCESS", paymentStatusesAtSave.get(1));
        assertEquals(List.of("UPI", "UPI"), paymentMethodsAtSave);
        assertEquals(List.of(new BigDecimal("220.00"), new BigDecimal("220.00")), paymentAmountsAtSave);
        assertTrue(transactionIdsAtSave.stream().allMatch(value -> value == null));
        assertTrue(paidAtValuesAtSave.stream().allMatch(value -> value == null));
        verify(paymentProcessor).processPayment("UPI", new BigDecimal("220.00"));

        assertEquals(10, inventory.getQuantity());
        verify(inventoryRepository, never()).saveAll(any());
        assertEquals(CartStatus.ACTIVE, cart.getStatus());
        verify(cartRepository, never()).save(any(Cart.class));

        verify(orderItemRepository).saveAll(any());
        assertEquals(1, savedItems.size());
        assertEquals(product, savedItems.get(0).getProduct());
        assertEquals(2, savedItems.get(0).getQuantity());
        assertEquals(new BigDecimal("110.00"), savedItems.get(0).getUnitPrice());
        assertEquals(new BigDecimal("220.00"), savedItems.get(0).getSubtotal());
        verify(productRepository, never()).save(any(Product.class));
    }
}
