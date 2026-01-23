package org.e4s.client.test;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.hazelcast.client.impl.clientside.HazelcastClientProxy;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.internal.serialization.impl.HeapData;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;

import org.apache.commons.io.IOUtils;
import org.e4s.configuration.model.CachePayload;
import org.e4s.configuration.model.MeterPQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// For JUnit 4 @RunWith(SpringRunner.class), or use @ExtendWith(SpringExtension.class) for JUnit 5
@State(Scope.Benchmark) // Define the scope of the benchmark state
@BenchmarkMode(Mode.Throughput) // Specify the benchmark mode (e.g., AverageTime, Throughput)
@OutputTimeUnit(TimeUnit.SECONDS) // Define the output time unit
@Threads(8)
public class CacheBenchmarking {


    private static HazelcastInstance instance;

    private static IMap<UUID, CachePayload<MeterPQ>> objectMap;
    private static RedissonClient redissonClient;
    private static Map<UUID, CachePayload<MeterPQ>> data = new HashMap<>();
    private static RMapCache<UUID, CachePayload<MeterPQ>> redisCacheMap;
    private static SerializationService serializationService;
    private static List<UUID> fileNames;

    private static UUID[] keys;

    private long start = 1737590400000L;
    private long FIVE_MINS = 5 * 60 * 1000L;

    @Autowired
    public void injectInstance(HazelcastInstance instance, RedissonClient redissonClient) {
        CacheBenchmarking.instance = instance;
        CacheBenchmarking.redissonClient = redissonClient;
        CacheBenchmarking.objectMap = instance.getMap("pq_cache_objects");
        CacheBenchmarking.redisCacheMap = redissonClient.getMapCache("pq_cache");
        // serializer
        CacheBenchmarking.serializationService = ((HazelcastClientProxy) instance).getSerializationService();
    }

    @Setup(Level.Trial)
    public void setup() throws IOException {
        // Optional: Perform setup before all benchmark iterations
        if (data.isEmpty()) {
            IntStream.range(0, 100).forEachOrdered(w -> {
                final UUID key = UUID.randomUUID();

                final CachePayload<MeterPQ> pq = new CachePayload<>();

                for (int i = 0; i < (288 * 21); i++) {
                    pq.add(new MeterPQ(key, 255, 1, start + (FIVE_MINS * (i))));
                }
                // dummy data
                data.put(key, pq);
            });
            // update keys
            CacheBenchmarking.keys = data.keySet().toArray(new UUID[0]);
        }

        if (fileNames == null) {
            fileNames = new ArrayList<>();
            Files.walk(Paths.get("/Users/ericwang/Workspace/projects/e4s-hz/client/src/test/read/"))
                .sorted(Comparator.reverseOrder()) // Delete deepest files first
                .forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        fileNames.add(UUID.fromString(path.getFileName().toString()));
                    }
                });
        }

    }

    // Hazelcast IMap
    @Benchmark
    public void _11_CacheIMapFilling() {
        // get a random key
        UUID key = randomElement(keys);
        // find data
        CachePayload<MeterPQ> payload = data.get(key);
        // upload to IMap
        objectMap.set(key, payload);
    }

    @Benchmark
    public void _12_CacheIMapReading() {
        // get a random key
        UUID key = randomElement(keys);
        CachePayload<MeterPQ> payload = objectMap.get(key);
    }

//    @Benchmark
    public void _21_cacheRedisFilling() {
        // pickup random key to do the put or set
        final UUID key = randomElement(keys);
        // upload
        redisCacheMap.put(key, data.get(key), 5, TimeUnit.MINUTES);
    }

//    @Benchmark
    public void _22_cacheRedisReading() {
        // pickup random key to do the put or set
        final UUID key = randomElement(keys);
        CachePayload<MeterPQ> data = redisCacheMap.get(key);
    }

    @Benchmark
    public void _31_cacheFileFilling() throws IOException {
        final UUID key = randomElement(keys);
        Data result = serializationService.toData(data.get(key));
        byte[] bytes = result.toByteArray();

        Path filePath = Path.of("/Users/ericwang/Workspace/projects/e4s-hz/client/src/test/output/" + key.toString());
        try {
            Files.write(filePath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
    }

    @Benchmark
    public void _32_cacheFileReading() {
        final UUID key = randomElement(CacheBenchmarking.fileNames.toArray(new UUID[0]));
        try (FileInputStream fin = new FileInputStream(
            "/Users/ericwang/Workspace/projects/e4s-hz/client/src/test/read/" + key.toString())) {
            byte[] payload = IOUtils.toByteArray(fin);
            Data f = new HeapData(payload);
            CachePayload<MeterPQ> fs = serializationService.toObject(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Benchmark
    public void _41_cacheHZMultiMapFilling() {
        final UUID key = randomElement(CacheBenchmarking.fileNames.toArray(new UUID[0]));
        // find data
        List<MeterPQ> payload = data.get(key).getAll();


    }


    public static <T> T randomElement(T[] keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("Keys must not be empty");
        }
        Random rnd = new Random();                    // one per method call
        int idx = rnd.nextInt(keys.length);           // 0 <= idx < size
        return keys[idx];
    }

    @TearDown(Level.Trial)
    public void teardown() throws InterruptedException, IOException {
        // Optional: Perform cleanup after all benchmark iterations
        // cleanup files
    }

    @Test
    public void runner() throws RunnerException, IOException {
        Options opt = new OptionsBuilder()
            .include("\\." + CacheBenchmarking.class.getSimpleName() + "\\.")
            .warmupIterations(2)
            .warmupTime(TimeValue.seconds(10))
            .measurementIterations(3)
//            .measurementTime(TimeValue.seconds(10))
            .forks(0) //0 makes debugging possible
            .shouldFailOnError(true)
//            .addProfiler(GCProfiler.class)
            .addProfiler(AsyncProfiler.class, "libPath=/Users/ericwang/Workspace/tools/async-profiler-4.2.1-macos/lib/libasyncProfiler.dylib;output=jfr;dir=./async-profiles/") // Add the profiler
            .build();

        new Runner(opt).run();
        // cleanup
        Files.walk(Paths.get("/Users/ericwang/Workspace/projects/e4s-hz/client/src/test/output/"))
            .sorted(Comparator.reverseOrder()) // Delete deepest files first
            .forEach(path -> {
                try {
                    if (Files.isRegularFile(path)) {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to delete " + path + ": " + e.getMessage());
                }
            });

    }


}
