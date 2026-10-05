package com.sourabh.selfcheckout.Repo;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Enum.CartStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer_CustomerIdAndStatus(Long customerId, CartStatus status);

    List<Cart> findByCustomer_CustomerIdAndStatusOrderByCreatedAtDesc(
            Long customerId, CartStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cart from Cart cart where cart.customer.customerId = :customerId and cart.status = :status")
    Optional<Cart> findByCustomerIdAndStatusForUpdate(
            @Param("customerId") Long customerId,
            @Param("status") CartStatus status);
}

