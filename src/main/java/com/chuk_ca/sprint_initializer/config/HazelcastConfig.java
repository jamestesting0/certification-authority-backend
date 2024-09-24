package com.chuk_ca.sprint_initializer.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setInstanceName("hazelcast-instance");

        config.getMapConfig("x509CertificateCache")
                .setBackupCount(1);

        return Hazelcast.newHazelcastInstance(config);
    }
}
