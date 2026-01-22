package org.e4s.configuration.client;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.e4s.configuration.operation.RunTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextMapConfig {


    @Bean
    public TextMapPropagator textMapPropagator(final OpenTelemetry openTelemetry) {
        return openTelemetry.getPropagators().getTextMapPropagator();
    }

    @Bean
    public TextMapSetter<RunTask> runTaskSetter() {
        return RunTask::setAttribute;
    }


}
