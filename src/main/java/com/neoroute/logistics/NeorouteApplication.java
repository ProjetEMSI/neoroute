package com.neoroute.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NeorouteApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeorouteApplication.class, args);
	}

}
