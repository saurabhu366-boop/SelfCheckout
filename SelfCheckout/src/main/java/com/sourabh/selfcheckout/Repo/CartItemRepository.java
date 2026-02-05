package com.sourabh.selfcheckout.Repo;
import com.sourabh.selfcheckout.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {}
