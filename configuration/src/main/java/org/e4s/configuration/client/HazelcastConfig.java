package org.e4s.configuration.client;

import com.hazelcast.client.config.ClientConfig;

import info.jerrinot.subzero.SubZero;
import org.e4s.configuration.model.MeterPQ;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Bean
    public ClientConfig createHzConfig(){
        ClientConfig config = new ClientConfig();
        config.setClusterName("e4s");
        config.setInstanceName("loadAll");
        config.getNetworkConfig().addAddress("localhost:6701");
        config.addLabel(""); // labels

        // serialization
        SubZero.useAsGlobalSerializer(config);
//        SubZero.useForClasses(config, MeterPQ.class);


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


