package com.joshrap.liteweight.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultStatus<T> {
    private boolean success;
    private boolean outDatedVersion;
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

    public boolean isFailure() {
        return !this.success;
    }
}
