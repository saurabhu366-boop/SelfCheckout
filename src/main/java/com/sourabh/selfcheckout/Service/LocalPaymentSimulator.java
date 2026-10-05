package com.sourabh.selfcheckout.Service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Local demo approval only; this component does not contact or charge a gateway. */
@Component
public class LocalPaymentSimulator implements PaymentProcessor {

    @Override
    public boolean processPayment(String paymentMethod, BigDecimal amount) {
        return true;
    }
}
