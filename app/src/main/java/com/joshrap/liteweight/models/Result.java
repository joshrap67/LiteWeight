package com.joshrap.liteweight.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    private boolean success;
    private String errorMessage;
    private T data;

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }

    public Result() {
        this.success = true;
    }

    public boolean isFailure() {
        return !this.success;
    }
}
