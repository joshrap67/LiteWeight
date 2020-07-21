package com.joshrap.liteweight.models;

import androidx.annotation.NonNull;

public class ResultStatus<T> {
    private boolean success;
    private boolean networkError;
    private String errorMessage;
    private T data;

    public ResultStatus(T data) {
        // constructor used if it was successful
        this.success = true;
        this.data = data;
        this.networkError = false;
    }

    public ResultStatus(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public ResultStatus(boolean networkError) {
        this.networkError = true;
        this.success = false;
        this.errorMessage = "Please check your internet connection.";
    }

    public ResultStatus() {
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isNetworkError() {
        return networkError;
    }

    public void setNetworkError(boolean networkError) {
        this.networkError = networkError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        return "Success: " + this.success + " networkError: " + this.networkError
                + " errorMessage: " + this.errorMessage + " Data: " + this.data;
    }
}
