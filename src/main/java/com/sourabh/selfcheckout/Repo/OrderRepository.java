package com.sourabh.selfcheckout.Repo;

import com.sourabh.selfcheckout.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer_CustomerIdOrderByCreatedAtDescOrderIdDesc(Long customerId);
}
