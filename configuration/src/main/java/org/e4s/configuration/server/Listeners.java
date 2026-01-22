package org.e4s.configuration.server;

import com.hazelcast.client.impl.ClientLifecycleMonitor;
import com.hazelcast.core.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class Listeners {

    private Logger LOG = LoggerFactory.getLogger(Listeners.class);

    @Bean
    public LifecycleListener lifecycleListener() {

        return lifecycleEvent -> {
            LOG.info("cluster: {}", lifecycleEvent);
        };
    }


}
