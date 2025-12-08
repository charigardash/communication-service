package com.learning.communication_service;

import com.common.base.BaseAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(
		exclude = {
				RedisAutoConfiguration.class
		}
)
@Import(BaseAutoConfiguration.class)
public class CommunicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunicationServiceApplication.class, args);
	}

}
