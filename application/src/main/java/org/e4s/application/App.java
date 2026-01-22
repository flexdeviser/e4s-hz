package org.e4s.application;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.unit.DataSize;

@SpringBootApplication()
@ComponentScan(basePackages = {"org.e4s.application", "org.e4s.configuration.server", "org.e4s.configuration.otl"})
public class App implements CommandLineRunner {

    final Logger LOG_STATS = LoggerFactory.getLogger("e4s-imap-stats");

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public UUID registerClientEventListener(final HazelcastInstance instance) {

        final Logger LOG = LoggerFactory.getLogger("e4s-client-listener");

        return instance.getClientService().addClientListener(new ClientListener() {
            @Override
            public void clientConnected(Client client) {
                LOG.info("client: {} connected", client);
            }

            @Override
            public void clientDisconnected(Client client) {
                LOG.info("client: {} disconnected", client);
            }
        });
    }

    @Autowired
    private HazelcastInstance hazelcastInstance;


    @Override
    public void run(String... args) throws Exception {

//        MapConfig expiringConfig = new MapConfig("expiring-map");
////        expiringConfig.setTimeToLiveSeconds(10);
//        expiringConfig.setMaxIdleSeconds(10);
//        EntryListenerConfig entryListenerConfig = new EntryListenerConfig();
//        entryListenerConfig.setImplementation(new IMapEventListener());
//        expiringConfig.addEntryListenerConfig(entryListenerConfig);
//        hazelcastInstance.getConfig().addMapConfig(expiringConfig);
//
//        Map<String, MapConfig> configs = hazelcastInstance.getConfig().getMapConfigs();
//
//        // Create a IMap and make the eviction base on time
//        IMap<String, String> map = hazelcastInstance.getMap("expiring-map");
//        map.addEntryListener((EntryEvictedListener<String, String>) event -> System.out.println("~"), true);
//
//        map.put("eric", "wang");
//
//        // Wait for the TTL to expire
//        Thread.sleep(15000); // Sleep a bit longer than TTL
//
//        String eric = map.get("eric");
//
//        System.out.println("~");

        // worker, print IMap stats.
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            IMap<Object, Object> cacheMap = hazelcastInstance.getMap("pq_cache_objects");
            LocalMapStats stats = cacheMap.getLocalMapStats();
            LOG_STATS.info("IMap: {} items: {}, cost: {} MB", "pq_cache_objects", stats.getOwnedEntryCount(),
                           DataSize.ofBytes(stats.getOwnedEntryMemoryCost()).toMegabytes());
            LOG_STATS.info("IMap: {} backup items: {}, cost: {} MB", "pq_cache_objects", stats.getBackupEntryCount(),
                           DataSize.ofBytes(stats.getBackupEntryMemoryCost()).toMegabytes());

        }, 10, 10, TimeUnit.SECONDS);


    }
}