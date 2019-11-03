package com;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }



    @Bean
    public CommandLineRunner demo() {

        return (args) -> {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setGroupConfig(new GroupConfig("strongbox","password"));
            HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

            IMap<Object, Object> test = client.getMap("test");
            test.put(1,1);
            System.out.println("size ---->" + test.size());

        };
    }
}
