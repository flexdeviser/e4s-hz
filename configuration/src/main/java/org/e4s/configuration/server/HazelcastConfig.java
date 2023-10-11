package org.e4s.configuration.server;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.spring.context.SpringManagedContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

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

        return config;
    }
}
