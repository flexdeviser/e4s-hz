package org.e4s.application.config;

import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hazelcast.core.HazelcastInstance;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ListenerProvider {


    @Bean
    public UUID registerClientEventListener(final HazelcastInstance instance){

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


}
