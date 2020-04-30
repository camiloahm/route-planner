package com.jumbo.routeplanner.domain;

import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.BDDAssertions.then;

public class MultipleOriginsToSingleDestinationRoutesTest {
    @Test
    public void shouldBeInvalidWithZeroOrigin() {
        testValidWithNrOfOrigins(0, false);
    }

    @Test
    public void shouldBeValidWithOneOrigin() {
        testValidWithNrOfOrigins(1, true);
    }

    @Test
    public void shouldBeValidWith19Origins() {
        testValidWithNrOfOrigins(19, true);
    }

    @Test
    public void shouldBeInvalidWith20Origins() {
        testValidWithNrOfOrigins(20, false);
    }

    @Test
    public void shouldBeInvalidWith21Origins() {
        testValidWithNrOfOrigins(21, false);
    }

    private void testValidWithNrOfOrigins(int nrOfOrigins, boolean shouldBeValid) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        MultipleOriginsToSingleDestinationRoutes query =
                MultipleOriginsToSingleDestinationRoutes.builder()
                    .origins(
                        IntStream.rangeClosed(1, 21)
                        .mapToObj(i -> new LatLong((double) i, (double) i))
                        .collect(Collectors.toList())
                    )
                    .destination(new LatLong(1.0, 1.0))
                    .build();

        then(validator.validate(query).size()).isEqualTo(1);
    }
}