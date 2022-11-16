package org.e4s.configuration.client;

import com.hazelcast.client.config.ClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Value("${cluster.name:e4s}")
    private String clusterName;

    @Value("${instance.name:${random.value}}")
    private String instanceName;

    @Bean
    public ClientConfig createHzConfig(){
        ClientConfig config = new ClientConfig();
        config.setClusterName(clusterName);
        config.setInstanceName(instanceName);
        config.getNetworkConfig().addAddress("localhost:6701");
        return config;
    }


}


