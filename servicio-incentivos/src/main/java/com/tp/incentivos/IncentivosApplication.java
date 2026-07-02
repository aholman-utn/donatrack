package com.tp.incentivos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        },
        scanBasePackages = {
                "com.tp.incentivos",
                "com.tp.donatrack.notificaciones",
                "com.tp.commons"
        }
)
public class IncentivosApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncentivosApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}