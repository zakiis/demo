package com.zakiis.rabbitmq.demo;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import com.zakiis.rabbitmq.demo.domain.constant.NotificationConstants;

@SpringBootTest
class RabbitmqDemoApplicationTests {
	
	@Resource(name = "notificationRabbitTemplate")
	RabbitTemplate rabbitTemplate;

	@Test
	void contextLoads() {
		sendPush();
	}
	
	void sendPush() {
		String content = "push content";
		rabbitTemplate.convertAndSend(NotificationConstants.EXCHANGE_NOTIFICATION
				, NotificationConstants.ROUTING_KEY_PUSH, content);
	}

}
