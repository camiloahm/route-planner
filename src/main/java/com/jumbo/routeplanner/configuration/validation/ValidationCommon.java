package com.jumbo.routeplanner.configuration.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.routeplanner.domain.exception.FailedToSerializeException;
import com.jumbo.routeplanner.domain.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ValidationCommon {
    private final Validator validator;
    private final ObjectMapper objectMapper;

    public <T> void validate(T data) {
        final Set<ConstraintViolation<T>> violations = validator.validate(data);

        if (!violations.isEmpty()) {
            Set<ValidationError> detailErrors = violations
                    .stream()
                    .map(validation -> ValidationError.builder()
                            .message(validation.getMessage())
                            .field(validation.getPropertyPath().toString())
                            .build())
                    .collect(Collectors.toSet());

            throw new ValidationException(serialize(detailErrors));
        }
    }

    private String serialize(final Set validationErrors) {
        try {
            return objectMapper.writeValueAsString(validationErrors);
        } catch (JsonProcessingException e) {
            throw new FailedToSerializeException(e);
        }
    }
}
