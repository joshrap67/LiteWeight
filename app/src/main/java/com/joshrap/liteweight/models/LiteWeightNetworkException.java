package com.joshrap.liteweight.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LiteWeightNetworkException extends Exception {

    private String errorType;

    public LiteWeightNetworkException(String errorType, String errorMessage) {
        super(errorMessage);
        this.errorType = errorType;
    }
}
