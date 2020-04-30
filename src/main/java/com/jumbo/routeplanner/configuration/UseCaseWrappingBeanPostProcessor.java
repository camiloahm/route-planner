package com.jumbo.routeplanner.configuration;

import com.jumbo.routeplanner.configuration.validation.ValidationCommon;
import com.jumbo.routeplanner.usecase.UseCase;
import com.jumbo.routeplanner.usecase.wrapper.DurationMetricsUseCaseWrapper;
import com.jumbo.routeplanner.usecase.wrapper.LoggingUseCaseWrapper;
import com.jumbo.routeplanner.usecase.wrapper.ValidationUseCaseWrapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UseCaseWrappingBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    @Lazy
    private MeterRegistry meterRegistry;

    @Autowired
    private ValidationCommon validationCommon;

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(@Nullable final Object bean, final String beanName) throws BeansException {
        if (bean instanceof UseCase<?, ?>) {
            log.debug("Wrapping {}.", beanName);
            return new LoggingUseCaseWrapper(
                    new ValidationUseCaseWrapper(
                            new DurationMetricsUseCaseWrapper((UseCase<?, ?>) bean, meterRegistry), validationCommon));
        }
        if (bean instanceof MeterRegistry) {
            final MeterRegistry registry = (MeterRegistry) bean;
            registry.config().commonTags("role", "routeplanner");
        }

        return bean;
    }
}
