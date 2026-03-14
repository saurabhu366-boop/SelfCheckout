package com.sourabh.selfcheckout.Repo;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Enum.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Loads the cart together with its items and each item's product in a single
     * SQL query (LEFT JOIN FETCH). Without this, Hibernate issues one extra SELECT
     * per item to load the eagerly-fetched Product — the classic N+1 problem.
     */
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.items ci " +
           "LEFT JOIN FETCH ci.product " +
           "WHERE c.userId = :userId AND c.status = :status")
    Optional<Cart> findByUserIdAndStatus(@Param("userId") String userId,
                                         @Param("status") CartStatus status);
}

