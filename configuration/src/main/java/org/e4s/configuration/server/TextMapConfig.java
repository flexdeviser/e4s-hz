package org.e4s.configuration.server;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import javax.annotation.Nullable;
import org.e4s.configuration.model.Payload;
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
    public TextMapGetter<RunTask> runTaskTraceGetter() {
        return new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(RunTask runTask) {
                return (Iterable<String>) runTask.getKeys().asIterator();
            }

            @Nullable
            @Override
            public String get(@Nullable RunTask runTask, String s) {
                return runTask.getAttribute(s);
            }
        };
    }

    @Bean
    public TextMapSetter<Payload> payloadTraceSetter() {
        return Payload::setAttribute;
    }

    @Bean
    public TextMapGetter<Payload> payloadTraceGetter() {
        return new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(Payload payload) {
                return (Iterable<String>) payload.getKeys().asIterator();
            }

            @Nullable
            @Override
            public String get(@Nullable Payload payload, String s) {
                return payload.getAttribute(s);
            }
        };
    }


}
