package com.sourabh.selfcheckout.Exception;


public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String msg) {
        super(msg);
    }
}

