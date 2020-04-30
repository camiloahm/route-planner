package com.jumbo.routeplanner.usecase.wrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.routeplanner.configuration.validation.ValidationCommon;
import com.jumbo.routeplanner.domain.exception.ValidationException;
import com.jumbo.routeplanner.usecase.UseCase;
import lombok.Data;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ValidationUseCaseWrapperTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ValidationUseCaseWrapper validationUseCaseWrapper;
    private UseCase decorated;

    @Before
    public void setupTest() {
        decorated = mock(UseCase.class);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ObjectMapper objectMapper = new ObjectMapper();
        ValidationCommon validationCommon = new ValidationCommon(validator, objectMapper);
        validationUseCaseWrapper = new ValidationUseCaseWrapper(decorated, validationCommon);
    }


    @Test
    public void executeThrowsIllegalArgumentExceptionWhenQueryIsNull() {
        // Act
        expectedException.expect(IllegalArgumentException.class);
        validationUseCaseWrapper.execute(null);
    }

    @Test
    public void executeThrowsValidationExceptionWhenQueryIsNotValid() {
        @Data
        class MyQuery {
            @NotNull
            String someProperty;
        }

        // Act
        expectedException.expect(ValidationException.class);
        validationUseCaseWrapper.execute(new MyQuery());
    }

    @Test
    public void executeDelegatesToDecoratedWhenQueryIsValid() {
        class SomeQuery {
        }
        // Given
        Object expectedResult = 1;
        when(decorated.execute(any())).thenReturn(expectedResult);

        // When
        Object actualResult = validationUseCaseWrapper.execute(new SomeQuery());

        // Then
        verify(decorated, times(1)).execute(any());
        then(actualResult).isSameAs(expectedResult);
    }
}