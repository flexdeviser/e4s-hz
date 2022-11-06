package org.e4s.configuration.client;

import com.hazelcast.client.config.ClientConfig;
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
        return config;
    }


}


