package com.jumbo.routeplanner.configuration.validation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ValidationError {

    private String field;
    private String message;
}
