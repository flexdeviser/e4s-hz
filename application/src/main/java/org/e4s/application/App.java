package org.e4s.application;

import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.UUID;

@SpringBootApplication
@ComponentScan(basePackages = {"org.e4s.application", "org.e4s.configuration.server"})
public class App implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

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


    @Override
    public void run(String... args) throws Exception {


    }
}