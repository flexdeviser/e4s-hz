package org.e4s.configuration.server;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.LifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class HazelcastConfig {


    @Value("${cluster.name:e4s}")
    private String clusterName;
    @Value("${instance.name:${random.value}}")
    private String instanceName;

    @Value("${app.port:6701}")
    private int port;

    @Autowired
    private LifecycleListener lifecycleListener;

    @Bean
    public Config createHzInstance() {
        Config config = new Config();
        config.setInstanceName(instanceName);
        config.setClusterName(clusterName);
        config.getNetworkConfig().setPort(port).setPortCount(100).setPortAutoIncrement(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(true);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("localhost");
        config.addListenerConfig(new ListenerConfig(lifecycleListener));
        return config;
    }
}
