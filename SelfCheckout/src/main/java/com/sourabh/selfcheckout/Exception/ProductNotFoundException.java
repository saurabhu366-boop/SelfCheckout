package com.sourabh.selfcheckout.Exception;


public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String msg) {
        super(msg);
    }
}

