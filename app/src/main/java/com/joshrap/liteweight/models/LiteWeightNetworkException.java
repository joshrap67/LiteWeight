package com.joshrap.liteweight.models;

public class LiteWeightNetworkException extends Exception {

    private String errorType;
    private int statusCode; // todo probably can delete

    public String getErrorType() {
        return errorType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public LiteWeightNetworkException(int statusCode, String errorType, String errorMessage) {
        super(errorMessage);
        this.errorType = errorType;
    }

    public LiteWeightNetworkException(int statusCode, String errorMessage) {
        super(errorMessage);
    }
}
