package com.guatequeriders.api.payments.exception;

public class PaymentProviderException extends RuntimeException {

    public PaymentProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
