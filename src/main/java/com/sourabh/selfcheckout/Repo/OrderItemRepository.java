package com.sourabh.selfcheckout.Repo;

import com.sourabh.selfcheckout.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select item
            from OrderItem item
            join fetch item.product
            where item.order.orderId in :orderIds
            order by item.order.orderId asc, item.orderItemId asc
            """)
    List<OrderItem> findWithProductsByOrderIds(@Param("orderIds") Collection<Long> orderIds);
}
