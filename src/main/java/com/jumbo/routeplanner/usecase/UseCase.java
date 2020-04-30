package com.jumbo.routeplanner.usecase;

@FunctionalInterface
public interface UseCase<REQUEST_TYPE, RESPONSE_TYPE> {
    RESPONSE_TYPE execute(REQUEST_TYPE requestTYPE);
}
