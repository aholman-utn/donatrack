package com.tp.donatrack.logistica.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoResetConfig {

    @Bean
    @Profile("local")
    public CommandLineRunner resetDatabase(MongoTemplate mongoTemplate) {
        return args -> {
            mongoTemplate.getDb().drop();
            System.out.println(">>> MongoDB database dropped for local profile");
        };
    }
}
