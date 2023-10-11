package org.e4s.client;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.time.Instant;
import java.util.concurrent.Future;
import org.e4s.configuration.operation.RunTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.e4s.client", "org.e4s.configuration.client", "org.e4s.configuration.otl"})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


    @Autowired
    private HazelcastInstance instance;

    @Autowired
    private OpenTelemetry openTelemetry;

    @Autowired
    private TextMapPropagator textMapPropagator;

    @Autowired
    private TextMapSetter<RunTask> runTaskSetter;


    @Autowired
    private Tracer tracer;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        Logger LOG = LoggerFactory.getLogger(ctx.getApplicationName());
        return args -> {
            // check if any other loader or pump running?
            IExecutorService exeService = instance.getExecutorService("stream");
            while(true){
                Span span = tracer.spanBuilder("operation").startSpan();

                try (Scope scope = span.makeCurrent()) {
                    for (int i = 0; i < 10; i++) {
                        Span job = tracer.spanBuilder("jobs").addLink(span.getSpanContext()).startSpan();
                        job.addEvent("create task", Instant.now());
                        RunTask runTask = new RunTask();

                        job.setStatus(StatusCode.valueOf("Handover to backend"));
                        // bind trace id
                        textMapPropagator.inject(Context.current(), runTask, runTaskSetter);
                        Future<Boolean> result = exeService.submit(runTask);
                        job.end();
                    }
                } catch (Throwable t) {
                    span.recordException(t);
                    throw t;
                } finally {
                    span.end();
                }


                Thread.sleep(10000);

            }



        };
    }
}
