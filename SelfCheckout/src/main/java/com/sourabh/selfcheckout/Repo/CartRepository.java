package com.sourabh.selfcheckout.Repo;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Enum.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndStatus(String userId, CartStatus status);
}

