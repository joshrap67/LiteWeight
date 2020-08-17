package com.joshrap.liteweight.models;

import androidx.annotation.NonNull;

import lombok.Data;

@Data
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

    @NonNull
    @Override
    public String toString() {
        return "Success: " + this.success + " networkError: " + this.networkError
                + " errorMessage: " + this.errorMessage + " Data: " + this.data;
    }
}
