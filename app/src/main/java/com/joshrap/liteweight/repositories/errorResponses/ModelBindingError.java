package com.joshrap.liteweight.repositories.errorResponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelBindingError {

    private String property;
    private String message;
}
