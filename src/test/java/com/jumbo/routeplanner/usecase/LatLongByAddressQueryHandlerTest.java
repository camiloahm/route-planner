package com.jumbo.routeplanner.usecase;

import com.jumbo.routeplanner.domain.Country;
import com.jumbo.routeplanner.domain.LatLongByAddress;
import com.jumbo.routeplanner.gateway.RouteplannerGateway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class LatLongByAddressQueryHandlerTest {

    @Mock
    private RouteplannerGateway postalCodeGateway;

    @InjectMocks
    private GetLatLongByAddress getLatLongByAddress;

    @Test
    public void shouldNotAllowPostalCodeWithEmptySpacesAfterApplyRules() {
        // GIVEN an input with empty spaces in the postal code
        final LatLongByAddress query = LatLongByAddress.builder()
                .postalCode(" 5 6 1 6 ")
                .countryCode(Country.NL)
                .build();

        // WHEN I try to call the execute method
        getLatLongByAddress.execute(query);

        // THEN it should be passed to the gateway without spaces
        verify(postalCodeGateway, only()).getMapLocationByPostalCode("5616", Country.NL.toString(), null);
    }

    @Test
    public void shouldNormalizeBelgiumPostalCode() {

        // GIVEN an input with spaces in postalCode
        LatLongByAddress query = LatLongByAddress.builder()
                .street("Simple streetname")
                .postalCode(" 5 6 1 6  ")
                .countryCode(Country.BE)
                .build();

        // WHEN executing it
        getLatLongByAddress.execute(query);

        // THEN the empty spaces should be removed
        verify(postalCodeGateway, only()).getMapLocationByPostalCode("5616", Country.BE.toString(), "SIMPLE STREETNAME");
    }

    @Test
    public void shouldNormalizeBelgiumStreetName() {
        // GIVEN an input with spaces in postalCode
        LatLongByAddress query = LatLongByAddress.builder()
                .street("     Simple streetname 123 4 ")
                .postalCode(" 5 6 1 6     ")
                .countryCode(Country.BE)
                .build();

        // WHEN executing it
        getLatLongByAddress.execute(query);

        // THEN the emptySpaces should be removed and street name should be normalized
        verify(postalCodeGateway, only()).getMapLocationByPostalCode("5616", Country.BE.toString(), "SIMPLE STREETNAME 123 4");
    }

    @Test
    public void shouldNormalizeNetherlandsPostalCode() {

        // GIVEN an input with spaces in postalCode
        LatLongByAddress query = LatLongByAddress.builder()
                .street("Simple streetname 123")
                .postalCode(" 5 6 1 6 V d  ")
                .countryCode(Country.NL)
                .build();

        // WHEN executing it
        getLatLongByAddress.execute(query);

        // THEN the empty spaces should be removed
        verify(postalCodeGateway, only()).getMapLocationByPostalCode("5616VD", Country.NL.toString(), "SIMPLE STREETNAME 123");
    }

    @Test
    public void shouldNormalizeNetherlandsStreetName() {
        // GIVEN an input with spaces in postalCode
        LatLongByAddress query = LatLongByAddress.builder()
                .street("     Simple streetname 123 ")
                .postalCode(" 5 6 1 6 V D  ")
                .countryCode(Country.NL)
                .build();

        // WHEN executing it
        getLatLongByAddress.execute(query);

        // THEN the emptySpaces should be removed
        verify(postalCodeGateway, only()).getMapLocationByPostalCode("5616VD", Country.NL.toString(), "SIMPLE STREETNAME 123");
    }

    @Test
    public void shouldApplyRuleForEmptyNetherlandsStreet() {
        // GIVEN an input with spaces in postalCode
        LatLongByAddress query = LatLongByAddress.builder()
                .postalCode(" 5 6 1 6 v d  ")
                .countryCode(Country.NL)
                .build();

        // WHEN executing it
        getLatLongByAddress.execute(query);

        // THEN the emptySpaces should be removed
        verify(postalCodeGateway, only()).getMapLocationByPostalCode("5616VD", Country.NL.toString(), null);
    }

}