package com.sourabh.selfcheckout.Service;

import java.math.BigDecimal;

/** Determines whether the current local payment simulation approves checkout. */
public interface PaymentProcessor {

    boolean processPayment(String paymentMethod, BigDecimal amount);
}
