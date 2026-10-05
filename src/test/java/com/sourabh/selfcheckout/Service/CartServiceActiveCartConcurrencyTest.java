package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.CartResponse;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Entity.Customer;
import com.sourabh.selfcheckout.Entity.Inventory;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServiceActiveCartConcurrencyTest {

    @Test
    void sequentialFirstScansReuseTheActiveCartAfterCustomerLock() {
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
        customer.setEmail("scan-test@grozo.local");

        Product product = new Product();
        product.setId(2L);
        product.setBarcode("7622202832857");
        product.setName("CADBURY CELEBRATION");
        product.setMrp(new BigDecimal("110.00"));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);

        AtomicReference<Cart> activeCart = new AtomicReference<>();
        when(productRepository.findByBarcode(product.getBarcode())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct_Id(product.getId())).thenReturn(Optional.of(inventory));
        when(customerRepository.findByEmailForUpdate(customer.getEmail()))
                .thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerIdAndStatusForUpdate(999L, CartStatus.ACTIVE))
                .thenAnswer(invocation -> Optional.ofNullable(activeCart.get()));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            if (cart.getId() == null) {
                cart.setId(500L);
            }
            activeCart.set(cart);
            return cart;
        });

        CartService service = new CartService(
                productRepository, cartRepository, cartItemRepository, inventoryRepository,
                customerRepository, orderRepository, orderItemRepository, paymentRepository,
                paymentProcessor);

        CartResponse firstResponse = service.scanProduct(product.getBarcode(), customer.getEmail(), 1);
        Cart firstCart = activeCart.get();
        CartResponse secondResponse = service.scanProduct(product.getBarcode(), customer.getEmail(), 1);

        assertEquals(1, firstResponse.getItems().size());
        assertEquals(1, secondResponse.getItems().size());
        assertSame(firstCart, activeCart.get());
        assertEquals(1, activeCart.get().getItems().size());
        assertEquals(2, activeCart.get().getItems().get(0).getQuantity());
        assertEquals(CartStatus.ACTIVE, activeCart.get().getStatus());
        verify(customerRepository, times(2)).findByEmailForUpdate(customer.getEmail());
        verify(cartRepository, times(2))
                .findByCustomerIdAndStatusForUpdate(999L, CartStatus.ACTIVE);
        verify(cartRepository, times(2)).save(any(Cart.class));
        assertEquals(10, inventory.getQuantity());
    }
}
