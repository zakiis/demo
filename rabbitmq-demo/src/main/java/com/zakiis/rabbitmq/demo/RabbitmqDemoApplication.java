package com.zakiis.rabbitmq.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RabbitmqDemoApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(RabbitmqDemoApplication.class, args);
		Thread.sleep(3000L);
	}

}
