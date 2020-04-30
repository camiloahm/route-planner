package com.jumbo.routeplanner.domain;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.jumbo.routeplanner.domain.Country.BE;
import static com.jumbo.routeplanner.domain.Country.NL;
import static org.assertj.core.api.BDDAssertions.then;

public class LatLongByAddressTest {

    @Test
    public void shouldBeInvalidWhenBelgiumPostalCodeWIthoutStreet() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        LatLongByAddress query = LatLongByAddress.builder()
                .countryCode(BE)
                .postalCode("1234")
                .build();

        then(validator.validate(query).size()).isEqualTo(1);
    }

    @Test
    public void shouldNotAcceptNLPostalCodeWithout4NumbersAnd2Letters() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        List<LatLongByAddress> givenInvalidNLPostalcodesList = Arrays.asList(
                LatLongByAddress.builder().countryCode(NL).postalCode("     ").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("123456").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("NL").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("12345AV").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("12345 AV").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("1234A").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("1234A  ").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("ABCF12").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("123AV").build()
        );

        givenInvalidNLPostalcodesList.forEach(givenInvalidNLPostalcode -> {
            Set<ConstraintViolation<LatLongByAddress>> actualValidation = validator.validate(givenInvalidNLPostalcode);
            then(actualValidation.size()).isEqualTo(1);
            String actualErrorMessage = actualValidation.stream().findFirst().get().getMessage();
            then(actualErrorMessage).isEqualTo("Netherland address should have 4 numbers and 2 letters");
        });
    }

    @Test
    public void shouldAcceptNLPostalCodeWith4NumbersAnd2Letters() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        List<LatLongByAddress> givenValidNLPostalcodesList = Arrays.asList(
                LatLongByAddress.builder().countryCode(NL).postalCode("5616VD").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("1234ZZ").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("  9764 ZZ  ").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("1234     ZA").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("6565GH").build(),
                LatLongByAddress.builder().countryCode(NL).postalCode("1235  AV  ").build()
        );
        givenValidNLPostalcodesList.forEach(givenInvalidNLPostalcode -> {
            Set<ConstraintViolation<LatLongByAddress>> actualValidation = validator.validate(givenInvalidNLPostalcode);
            then(actualValidation.size()).isZero();
        });

    }

    @Test
    public void shouldNotAcceptBEPostalCodeWithout4Numbers() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        List<LatLongByAddress> givenInvalidNLPostalcodesList = Arrays.asList(
                LatLongByAddress.builder().countryCode(BE).postalCode("     ").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("123456").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("BE").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("12345AV").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode(" 12345 AV").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("1234A").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("1234A  ").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("ABCD").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("  ABCD  ").street("Any Street").build()
        );

        givenInvalidNLPostalcodesList.forEach(givenInvalidNLPostalcode -> {
            Set<ConstraintViolation<LatLongByAddress>> actualValidation = validator.validate(givenInvalidNLPostalcode);
            then(actualValidation.size()).isEqualTo(1);
            String actualErrorMessage = actualValidation.stream().findFirst().get().getMessage();
            then(actualErrorMessage).isEqualTo("Belgium address should have street and be composed of 4 numbers");
        });
    }

    @Test
    public void shouldAcceptBEPostalCodeWith4Numbers() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        List<LatLongByAddress> givenInvalidNLPostalcodesList = Arrays.asList(
                LatLongByAddress.builder().countryCode(BE).postalCode("  1221   ").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("2132").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode("9988 ").street("Any Street").build(),
                LatLongByAddress.builder().countryCode(BE).postalCode(" 1919    ").street("Any Street").build()
        );

        givenInvalidNLPostalcodesList.forEach(givenInvalidNLPostalcode -> {
            Set<ConstraintViolation<LatLongByAddress>> actualValidation = validator.validate(givenInvalidNLPostalcode);
            then(actualValidation.size()).isZero();
        });
    }

}