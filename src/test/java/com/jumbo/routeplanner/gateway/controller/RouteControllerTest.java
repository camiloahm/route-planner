package com.jumbo.routeplanner.gateway.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.routeplanner.domain.LatLong;
import com.jumbo.routeplanner.domain.MultipleOriginsToSingleDestinationRoutes;
import com.jumbo.routeplanner.domain.OriginToDestinationRoute;
import com.jumbo.routeplanner.domain.Route;
import com.jumbo.routeplanner.gateway.controller.advice.GenericExceptionHandler;
import com.jumbo.routeplanner.usecase.UseCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class RouteControllerTest {

    @Mock
    private UseCase<OriginToDestinationRoute, Optional<Route>> getRoute;

    @Mock
    private UseCase<MultipleOriginsToSingleDestinationRoutes, Collection<Route>> getMultipleOriginsRoute;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        RouteController routeController = new RouteController(getRoute, getMultipleOriginsRoute);
        mockMvc = MockMvcBuilders.standaloneSetup(routeController)
                .setControllerAdvice(new GenericExceptionHandler(objectMapper))
                .build();
    }

    @Test
    public void shouldRetrieveRouteForSingleValidQuery() throws Exception {
        // GIVEN a valid Query to calculate the route
        OriginToDestinationRoute given = OriginToDestinationRoute.builder()
                .destination(LatLong.builder().lat(1.0).lng(2.0).build())
                .origin(LatLong.builder().lat(3.0).lng(4.0).build())
                .build();

        // WHEN I try to get the route
        Route expected = Route.builder()
                .destination(LatLong.builder().lat(1.0).lng(2.0).build())
                .origin(LatLong.builder().lat(3.0).lng(4.0).build())
                .distanceInMeters(50.0)
                .timeInSeconds(300L)
                .build();

        when(getRoute.execute(given)).thenReturn(Optional.of(Route.builder()
                .destination(LatLong.builder().lat(1.0).lng(2.0).build())
                .origin(LatLong.builder().lat(3.0).lng(4.0).build())
                .distanceInMeters(50.0)
                .timeInSeconds(300L)
                .build()));

        mockMvc.perform(
                get("/routes")
                        .accept(APPLICATION_JSON_UTF8)
                        .param("destination.lat", "1.0")
                        .param("destination.lng", "2.0")
                        .param("origin.lat", "3.0")
                        .param("origin.lng", "4.0"))
                // THEN the route should be returned
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
        verify(getRoute, times(1)).execute(given);
    }

    @Test
    public void shouldReturnBadRequestDueToInvalidInput() throws Exception {
        // GIVEN an invalid valid Query to calculate the route

        // WHEN I try to get the route
        mockMvc.perform(
                get("/routes")
                        .accept(APPLICATION_JSON_UTF8)
                        .param("destination.lat", "1.0")
                        .param("destination.lng", "2.0"))
                // THEN a bad request should be returned
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                get("/routes")
                        .accept(APPLICATION_JSON_UTF8)
                        .param("origin.lat", "3.0")
                        .param("origin.lng", "4.0"))
                // THEN a bad request should be returned
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(getRoute, times(0)).execute(any());
    }

    @Test
    public void shouldReturnBadRequestDueToInvalidInputLatLongRange() throws Exception {
        // GIVEN an invalid valid Query to calculate the route

        // WHEN I try to get the route
        mockMvc.perform(
                get("/routes")
                        .accept(APPLICATION_JSON_UTF8)
                        .param("destination.lat", "91.0")
                        .param("destination.lng", "2.0")
                        .param("origin.lat", "3.0")
                        .param("origin.lng", "4.0"))
                // THEN a bad request should be returned
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                get("/routes")
                        .accept(APPLICATION_JSON_UTF8)
                        .param("destination.lat", "1.0")
                        .param("destination.lng", "2.0")
                        .param("origin.lat", "3.0")
                        .param("origin.lng", "181.0"))
                // THEN a bad request should be returned
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(getRoute, times(0)).execute(any());
    }


    @Test
    public void shouldRetrieveRouteForManyOriginsAndSingleDestination() throws Exception {
        // GIVEN a valid Query with 2 origins and 1 destination
        final LatLong givenOrigin1 = LatLong.builder().lat(1.0).lng(2.0).build();
        final LatLong givenOrigin2 = LatLong.builder().lat(3.0).lng(4.0).build();
        final LatLong givenDestination = LatLong.builder().lat(5.0).lng(6.0).build();
        MultipleOriginsToSingleDestinationRoutes given = MultipleOriginsToSingleDestinationRoutes.builder()
                .origins(Arrays.asList(givenOrigin1, givenOrigin2))
                .destination(givenDestination)
                .build();

        // WHEN I try to get the route for the 2 origins
        Route expectedForOrigin1 = Route.builder()
                .destination(LatLong.builder().lat(5.0).lng(6.0).build())
                .origin(LatLong.builder().lat(1.0).lng(2.0).build())
                .distanceInMeters(50.0)
                .timeInSeconds(300L)
                .build();

        Route expectedForOrigin2 = Route.builder()
                .destination(LatLong.builder().lat(5.0).lng(6.0).build())
                .origin(LatLong.builder().lat(3.0).lng(4.0).build())
                .distanceInMeters(66.0)
                .timeInSeconds(777L)
                .build();

        when(getMultipleOriginsRoute.execute(given)).
                thenReturn(Arrays.asList(expectedForOrigin1, expectedForOrigin2));

        final MvcResult mvcResult = mockMvc.perform(
                post("/routes")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(given)))
                // THEN the route should be calculated successfully
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        verify(getMultipleOriginsRoute, times(1)).execute(given);
        final Collection<Route> actualRoutes = fromJSON(new TypeReference<Collection<Route>>() {
        }, mvcResult.getResponse().getContentAsString());
        then(actualRoutes.size()).isEqualTo(2);
        then(actualRoutes).containsExactly(expectedForOrigin1, expectedForOrigin2);

    }

    @Test
    public void shouldReturnBadRequestDueToInvalidAmountOfOrigins() throws Exception {
        // GIVEN a valid Query with 21 origins and 1 destination
        final LatLong givenDestination = LatLong.builder().lat(5.5).lng(6.6).build();
        MultipleOriginsToSingleDestinationRoutes given = MultipleOriginsToSingleDestinationRoutes.builder()
                .origins(IntStream.range(0, 21).mapToObj(i -> LatLong.builder().lat((double) i).lng((double) i).build()).collect(Collectors.toList()))
                .destination(givenDestination)
                .build();

        // WHEN I try to get the route for the 21 origins
        mockMvc.perform(
                post("/routes")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(given)))
                // THEN a bad request should be returned
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(getMultipleOriginsRoute, never()).execute(any());


        given = MultipleOriginsToSingleDestinationRoutes.builder()
                .destination(givenDestination)
                .build();
        // WHEN I try to get the route for the NO origins
        mockMvc.perform(
                post("/routes")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(given)))
                // THEN a bad request should be returned
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(getMultipleOriginsRoute, never()).execute(any());
    }

    @Test
    public void shouldReturnNotFoundWhenNoRouteFound() throws Exception {
        // GIVEN a valid Query to calculate the route
        final LatLong givenOrigin1 = LatLong.builder().lat(1.0).lng(2.0).build();
        final LatLong givenOrigin2 = LatLong.builder().lat(3.0).lng(4.0).build();
        final LatLong givenDestination = LatLong.builder().lat(5.0).lng(6.0).build();
        MultipleOriginsToSingleDestinationRoutes given = MultipleOriginsToSingleDestinationRoutes.builder()
                .origins(Arrays.asList(givenOrigin1, givenOrigin2))
                .destination(givenDestination)
                .build();

        // WHEN I try to get the route for the 2 origins
        when(getMultipleOriginsRoute.execute(given)).
                thenReturn(Collections.emptyList());

        mockMvc.perform(
                post("/routes")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(given)))
                // THEN a bad request should be returned
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(getMultipleOriginsRoute, times(1)).execute(given);
    }

    private <T> T fromJSON(final TypeReference<T> type, final String jsonPacket) {
        T data;
        try {
            data = new ObjectMapper().readValue(jsonPacket, type);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return data;
    }
}