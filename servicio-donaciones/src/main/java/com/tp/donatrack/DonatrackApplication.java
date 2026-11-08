package com.tp.donatrack;

import com.tp.commons.config.CommonsSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.tp"})
@EnableScheduling
@Import(CommonsSecurityConfig.class)
public class DonatrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(DonatrackApplication.class, args);
	}

}
