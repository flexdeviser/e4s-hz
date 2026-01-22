package org.e4s.configuration.operation;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.context.SpringAware;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.e4s.configuration.model.Payload;
import org.e4s.configuration.service.Jobs;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringAware
public class RunTask implements Callable<Boolean>, ApplicationContextAware, Serializable {


    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private OpenTelemetry openTelemetry;

    @Autowired
    private TextMapPropagator textMapPropagator;
    @Autowired
    private TextMapGetter<RunTask> runTaskGetter;

    @Autowired
    private TextMapSetter<Payload> payloadTraceSetter;

    @Autowired
    private Tracer tracer;

    @Autowired
    private Jobs jobs;


    private final Properties properties = new Properties();

    @Override
    public Boolean call() throws Exception {
        // get trace id
        Context ctx = textMapPropagator.extract(Context.current(), this, runTaskGetter);
        Span span = tracer.spanBuilder("run job").setParent(ctx).setSpanKind(SpanKind.SERVER).startSpan();

        try (Scope scope = span.makeCurrent()) {
            System.out.println("run operation and transfer data into worker queue");
            Payload payload = new Payload();
            payload.add(span.getSpanContext().getTraceId());
            // bind trace context
            textMapPropagator.inject(Context.current(), payload, payloadTraceSetter);
            jobs.add(payload);
        } catch (Exception ex){
            ex.printStackTrace();
        }finally {
            span.end();
        }

        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }


    public void setAttribute(final String name, final String value) {
        properties.setProperty(name, value);
    }

    public String getAttribute(final String name) {
        return properties.getProperty(name, "");
    }

    public Enumeration<?> getKeys() {
        return properties.propertyNames();
    }


}
