package com.joshrap.liteweight.repositories.errorResponses;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BadRequestResponse {

    public String message;
    public String errorType;
    public List<ModelBindingError> requestErrors;
}
