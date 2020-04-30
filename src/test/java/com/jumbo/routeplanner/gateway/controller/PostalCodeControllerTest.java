package com.jumbo.routeplanner.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.LatLongByAddress;
import com.jumbo.routeplanner.gateway.controller.advice.GenericExceptionHandler;
import com.jumbo.routeplanner.usecase.UseCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class PostalCodeControllerTest {

    @Mock
    private UseCase<LatLongByAddress, Optional<LatLong>> getLatitudeAndLongitude;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        PostalCodeController postalCodeController = new PostalCodeController(getLatitudeAndLongitude);
        mockMvc = MockMvcBuilders.standaloneSetup(postalCodeController)
                .setControllerAdvice(new GenericExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldRetrieveLatAndLongForValidPostalCode() throws Exception {
        // GIVEN a valid postalCode
        String postalCode = "5616 VD";
        String country = "NL";

        // WHEN I try to get the lat and log
        LatLong expected = LatLong.builder()
                .lat(56.434232)
                .lng(5.3245)
                .build();
        when(getLatitudeAndLongitude.execute(any())).thenReturn(Optional.of(expected));

        mockMvc.perform(
                get("/postalcodes")
                        .accept(APPLICATION_JSON_UTF8)
                        .param("postalCode", postalCode)
                        .param("countryCode", country))
                // THEN the latitude should be retrieved successfully
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
        verify(getLatitudeAndLongitude, only()).execute(any());
    }

    @Test
    public void shouldReturnBadRequestDueToMissingCountryCode() throws Exception {
        // GIVEN an  invalid postalCode (missing country code)
        String postalCode = "5616 VD";

        // WHEN I try to get the lat and log
        mockMvc.perform(
                get("/postalcodes")
                        .accept(APPLICATION_JSON_UTF8)
                        .param("postalCode", postalCode))
                // THEN the 400 should be returned
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(getLatitudeAndLongitude, never()).execute(any());
    }
}
