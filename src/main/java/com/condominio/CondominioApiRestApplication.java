package com.condominio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CondominioApiRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CondominioApiRestApplication.class, args);
	}

}
