package com.tp.incentivos;

import com.tp.commons.config.CommonsSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        },
        scanBasePackages = {
                "com.tp.incentivos",
                "com.tp.commons"
        }
)
@Import(CommonsSecurityConfig.class)
public class IncentivosApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncentivosApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}