package org.e4s.configuration.client;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracerConfig {


    @Autowired
    private OpenTelemetry openTelemetry;

    @Bean
    public Tracer tracer(){
        return openTelemetry.getTracer("hz-operation-client", "0.1.0");
    }



}
