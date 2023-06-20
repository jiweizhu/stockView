package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class NotificationApplication {

	private static final Logger logger = LoggerFactory.getLogger(NotificationApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(NotificationApplication.class, args);
		logger.info("=========================+++++++++++++++==");
	}

}
