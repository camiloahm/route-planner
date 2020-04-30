package com.jumbo.routeplanner.configuration.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsHelper {
    public static <T> T recordDuration(final MeterRegistry meterRegistry, final Supplier<T> supplier, final String metricName, final String... tags) {
        return recordDuration(meterRegistry, supplier, metricName, obj -> Tags.of(tags));
    }

    public static <T> T recordDuration(final MeterRegistry meterRegistry, final Supplier<T> supplier, final String metricName, final Function<T, Tags> createTags) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean hasException = false;
        T result = null;
        try {
            result = supplier.get();
            return result;
        } catch (final Exception ex) {
            hasException = true;
            throw ex;
        } finally {
            stopWatch.stop();
            if (meterRegistry != null) {
                Tags tags = createTags.apply(result);
                final Tag exceptionTag = Tag.of("Exception", hasException ? "true" : "false");
                tags = tags == null ? Tags.of(exceptionTag) : tags.and(exceptionTag);

                final DistributionSummary summary = meterRegistry.summary(metricName, withContextTags(tags));
                summary.record(stopWatch.getTotalTimeMillis());
            }
        }
    }

    private static Tags addIfNotExists(final Tags tags, final String key, final String value) {
        return tags.stream().noneMatch(t -> t.getKey().equals(key)) ? tags.and(key, value) : tags;
    }

    private static Tags withContextTags(Tags tags) {
        tags = addIfNotExists(tags, "uri", getCurrentRequestURI());
        tags = addIfNotExists(tags, "method", getCurrentRequestMethod());
        tags = addIfNotExists(tags, "user-agent", getCurrentUserAgent());
        return tags;
    }

    private static Optional<HttpServletRequest> getCurrentHttpRequest() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            final HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return Optional.of(request);
        }
        return Optional.empty();
    }

    private static String getCurrentUserAgent() {
        return getCurrentHttpRequest()
                .map(req -> req.getHeader(HttpHeaders.USER_AGENT))
                .orElse("null");
    }

    private static String getCurrentRequestURI() {
        return getCurrentHttpRequest()
                .map(HttpServletRequest::getRequestURI)
                .orElse("null");
    }

    private static String getCurrentRequestMethod() {
        return getCurrentHttpRequest()
                .map(HttpServletRequest::getMethod)
                .orElse("null");
    }
}

