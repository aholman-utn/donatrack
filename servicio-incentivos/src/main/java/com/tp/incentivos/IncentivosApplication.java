package com.tp.incentivos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.tp.incentivos", "com.tp.donatrack.notificaciones", "com.tp.commons"})
public class IncentivosApplication {
    public static void main(String[] args) {
        SpringApplication.run(IncentivosApplication.class, args);
    }
}
