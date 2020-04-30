package com.jumbo.routeplanner.usecase.wrapper;

import com.jumbo.routeplanner.configuration.validation.ValidationCommon;
import com.jumbo.routeplanner.usecase.UseCase;
import lombok.RequiredArgsConstructor;

import static org.springframework.util.Assert.notNull;

@RequiredArgsConstructor
public class ValidationUseCaseWrapper<REQUEST_TYPE, RESPONSE_TYPE> implements UseCase<REQUEST_TYPE, RESPONSE_TYPE> {

    private final UseCase<REQUEST_TYPE, RESPONSE_TYPE> wrappedUseCase;
    private final ValidationCommon validationCommon;

    @Override
    public RESPONSE_TYPE execute(final REQUEST_TYPE query) {
        notNull(query, "query cannot be null");
        validationCommon.validate(query);
        return wrappedUseCase.execute(query);
    }
}
