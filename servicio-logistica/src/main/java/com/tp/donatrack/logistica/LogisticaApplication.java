package com.tp.donatrack.logistica;

import com.tp.commons.config.CommonsSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Import;
import com.tp.commons.config.RabbitMQCommonsConfig;

@SpringBootApplication
@Import({ RabbitMQCommonsConfig.class, CommonsSecurityConfig.class, com.tp.commons.config.ServiceToken.class })
public class LogisticaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticaApplication.class, args);
    }
}
