package com.joshrap.liteweight.repositories.errorResponses;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ModelBindingError {

    public String property;
    public String message;
}
