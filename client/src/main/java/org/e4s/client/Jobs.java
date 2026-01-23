package org.e4s.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.e4s.configuration.model.CachePayload;
import org.e4s.configuration.model.MeterPQ;
import org.e4s.configuration.operation.RunTask;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.e4s.client", "org.e4s.configuration.client", "org.e4s.configuration.otl"})
public class Jobs {

    public static void main(String[] args) {
        SpringApplication.run(Jobs.class, args);
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


    private RedissonClient redissonClient;

    private long start = 1737590400000L;
    private long FIVE_MINS = 5 * 60 * 1000L;

    @Autowired
    public void loadRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

//        List<MeterPQ> data = new ArrayList<>();
//        UUID key = UUID.randomUUID();
//        for (int i = 0; i < (288 * 21); i++) {
//            MeterPQ meterPq = new MeterPQ(key, 255, 1);
//            data.add(meterPq);
//        }
//
//        String info = GraphLayout.parseInstance(data).toFootprint();
//        System.out.println(info);

//        Logger LOG = LoggerFactory.getLogger(ctx.getApplicationName());
//        return jobs();
//        return cacheLoader();
//        return cacheLoaderInMemory();
//        return cacheRedisLoader();
        return args -> {
//            multiMapLoader();
        };
    }


    private void multiMapLoader() {

        List<UUID> keys = new ArrayList<>();

        MultiMap<UUID, MeterPQ> multiMap = instance.getMultiMap("pg_cache");



        IntStream.range(0, 10000).forEachOrdered(w -> {
            final UUID key = UUID.randomUUID();
            keys.add(key);
            List<MeterPQ> payload = new ArrayList<>();
            for (int i = 0; i < (288 * 21); i++) {
                MeterPQ meterPq = new MeterPQ(key, 255, 1, start + (FIVE_MINS * (i)));
                payload.add(meterPq);
            }
            CompletableFuture<Void> task = multiMap.putAllAsync(key, payload).toCompletableFuture();

            task.join();

            if (w % 1000 == 0) {
                System.out.println(w + " devices data loaded.");
            }
        });

        keys.forEach(key -> {
            Collection<MeterPQ> data = multiMap.get(key);
            System.out.println("Size: " + data.size());
        });


    }


    private @NonNull CommandLineRunner cacheLoader() {
        return args -> {
            List<UUID> keys = new ArrayList<>();
//            IMap<UUID, List<MeterPQ>> map = instance.getMap("pq_cache_objects");
            IMap<UUID, CachePayload> map = instance.getMap("pq_cache_objects");

            IntStream.range(0, 10000).forEachOrdered(w -> {
                final UUID key = UUID.randomUUID();
                keys.add(key);
//                List<MeterPQ> data = new ArrayList<>(); // original arraylist will use around 4G
                CachePayload<MeterPQ> cpl = new CachePayload<>(); // wrap arraylist to an object will only take 600mb

                for (int i = 0; i < (288 * 21); i++) {
                    MeterPQ meterPq = new MeterPQ(key, 255, 1, start + (FIVE_MINS * (i)));
//                    data.add(meterPq);
                    cpl.add(meterPq);
                }
                map.put(key, cpl);
            });

            // read from IMap
            keys.forEach(key -> {
                // tested, works.
                CachePayload result = map.get(key);
//                System.out.println("~" + result.getAll().size());
            });
        };
    }


    private @NonNull CommandLineRunner cacheRedisLoader() {
        return args -> {
            RMapCache<UUID, List<MeterPQ>> mapCache = redissonClient.getMapCache("pq_cache");
            IntStream.range(0, 10000).forEachOrdered(w -> {
                final UUID key = UUID.randomUUID();
                List<MeterPQ> data = new ArrayList<>();

                for (int i = 0; i < (288 * 21); i++) {
                    MeterPQ meterPq = new MeterPQ(key, 255, 1, start + (FIVE_MINS * (i)));
                    data.add(meterPq);
                }
                mapCache.put(key, data, 30, TimeUnit.SECONDS);
            });
        };
    }

    private @NonNull CommandLineRunner cacheLoaderInMemory() {
        return args -> {
            Map<UUID, List<MeterPQ>> map = new HashMap<>();

            IntStream.range(0, 10000).forEachOrdered(w -> {
                final UUID key = UUID.randomUUID();
                List<MeterPQ> data = new ArrayList<>();

                for (int i = 0; i < (288 * 21); i++) {
                    MeterPQ meterPq = new MeterPQ(key, 255, 1, start + (FIVE_MINS * (i)));
                    data.add(meterPq);
                }

                map.put(key, data);
            });

//            String info = GraphLayout.parseInstance(map).toFootprint();
//            System.out.println(info);

            System.out.println(map.size());

        };
    }


    private @NonNull CommandLineRunner jobs() {
        return args -> {
            // check if any other loader or pump running?
            IExecutorService exeService = instance.getExecutorService("stream");
            while (true) {
                Span span = tracer.spanBuilder("operation").startSpan();

                try (Scope scope = span.makeCurrent()) {
                    for (int i = 0; i < 10; i++) {
                        Span job = tracer.spanBuilder("jobs").addLink(span.getSpanContext()).startSpan();
                        job.addEvent("create task", Instant.now());
                        RunTask runTask = new RunTask();
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
