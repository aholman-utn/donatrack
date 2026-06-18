package com.tp.donatrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DonatrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(DonatrackApplication.class, args);
	}

}
