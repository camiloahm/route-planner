package com.jumbo.routeplanner.usecase.wrapper;

import com.jumbo.routeplanner.usecase.UseCase;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import static com.jumbo.routeplanner.configuration.metrics.MetricsHelper.recordDuration;

@RequiredArgsConstructor
public class DurationMetricsUseCaseWrapper<REQUEST_TYPE, RESPONSE_TYPE> implements UseCase<REQUEST_TYPE, RESPONSE_TYPE> {

    private final UseCase<REQUEST_TYPE, RESPONSE_TYPE> wrappedUseCase;
    private final MeterRegistry meterRegistry;

    @Override
    public RESPONSE_TYPE execute(final REQUEST_TYPE request) {
        final String requestType = request.getClass().getName();
        return recordDuration(meterRegistry, () -> wrappedUseCase.execute(request), "usecase.execute", "Request", requestType);
    }
}
