package org.e4s.configuration.server;

import com.hazelcast.config.CacheDeserializedValues;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.spring.context.SpringManagedContext;

import info.jerrinot.subzero.SubZero;
import org.e4s.configuration.model.MeterPQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {


    @Value("${app-name:dev}")
    private String appName;

    @Value("${app-port:6701}")
    private int port;

    @Autowired
    private LifecycleListener lifecycleListener;


    @Bean
    public SpringManagedContext managedContext() {
        return new SpringManagedContext();
    }

    @Bean
    public Config createHzInstance() {
        Config config = new Config();
        config.setInstanceName(appName);
        config.setClusterName("e4s");
        config.getNetworkConfig().setPort(port).setPortCount(100).setPortAutoIncrement(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(true);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("localhost");
        config.addListenerConfig(new ListenerConfig(lifecycleListener));
        config.setManagedContext(managedContext());

        // default IMap for testing
        MapConfig objectsConfig = new MapConfig("pq_cache_objects");
//        expiringConfig.setTimeToLiveSeconds(10);
        objectsConfig.setMaxIdleSeconds(120);
        objectsConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        objectsConfig.setCacheDeserializedValues(CacheDeserializedValues.NEVER);
        objectsConfig.setBackupCount(1);
        config.addMapConfig(objectsConfig);

        MapConfig bytesConfig = new MapConfig("pq_cache_bytes");
//        expiringConfig.setTimeToLiveSeconds(10);
        bytesConfig.setMaxIdleSeconds(120);
        bytesConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        bytesConfig.setCacheDeserializedValues(CacheDeserializedValues.NEVER);
        bytesConfig.setBackupCount(1);
        config.addMapConfig(bytesConfig);

        // serialization
        SubZero.useAsGlobalSerializer(config);
//        SubZero.useForClasses(config, MeterPQ.class);

        // Create the serializer configuration
//        SerializerConfig productSerializerConfig = new SerializerConfig()
//            .setTypeClass(MeterPQ.class) // The class to serialize
//            .setImplementation(new MeterPQSerializer()); // The custom serializer implementation
//
//        // Add it to the main serialization configuration
//        SerializationConfig serializationConfig = config.getSerializationConfig();
//        serializationConfig.addSerializerConfig(productSerializerConfig);

        return config;
    }
}
