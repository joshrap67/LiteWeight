package com.joshrap.liteweight.repositories.errorResponses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadRequestResponse {

    private String message;
    private String errorType;
    private List<ModelBindingError> requestErrors;
}
