package org.e4s.application.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.e4s.configuration.model.Payload;
import org.e4s.configuration.service.Jobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Office implements Jobs {

    final private ArrayBlockingQueue<Payload> payloadQueue;

    final ExecutorService execService;

    @Autowired
    private TextMapGetter<Payload> payloadTraceGetter;


    @Autowired
    private TextMapPropagator textMapPropagator;

    @Autowired
    private Tracer tracer;

    public Office() {
        execService = Executors.newWorkStealingPool(1);
        payloadQueue = new ArrayBlockingQueue<>(100);
        // start worker
        execService.submit(new Worker());
    }

    @Override
    public void add(Payload payload) {
        this.payloadQueue.offer(payload);
    }


    private class Worker implements Runnable {


        @Override
        public void run() {

            while (true) {
                try {

                    Payload job = payloadQueue.take();

                    // find trace id in each job
                    Context ctx = textMapPropagator.extract(Context.current(), job, payloadTraceGetter);

                    Span span = tracer.spanBuilder("worker").setParent(ctx).setSpanKind(SpanKind.SERVER)
                        .startSpan();

                    try (Scope scope = span.makeCurrent()) {
                        System.out.println("job found in queue and get processed, data: " + Arrays.toString(
                            job.getData().toArray()));
                    } finally {
                        span.end();
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }


        }
    }


}
