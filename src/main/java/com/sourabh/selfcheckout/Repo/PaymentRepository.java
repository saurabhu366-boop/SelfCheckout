package com.sourabh.selfcheckout.Repo;

import com.sourabh.selfcheckout.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
