package com.example.timi_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TimiApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimiApiApplication.class, args);
	}

}
