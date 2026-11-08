package com.tp.donatrack.notificaciones;

import com.tp.commons.config.CommonsSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(
        scanBasePackages = {
                "com.tp.donatrack.notificaciones",
                "com.tp.commons"
        }
)
@Import(CommonsSecurityConfig.class)
public class NotificacionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

