package com.neoroute.logistics.exception;

import lombok.Getter;

/**
 * Exception métier personnalisée pour les erreurs applicatives
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }
}
