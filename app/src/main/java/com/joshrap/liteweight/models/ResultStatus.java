package com.joshrap.liteweight.models;

import lombok.Data;

@Data
public class ResultStatus<T> {
    private boolean success;
    private String errorMessage;
    private T data;

    public ResultStatus(T data) {
        // constructor used if it was successful
        this.success = true;
        this.data = data;
    }

    public ResultStatus(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public ResultStatus() {
        this.success = false;
    }
}
