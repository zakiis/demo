package com.zakiis.rabbitmq.demo.message;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.zakiis.rabbitmq.demo.domain.constant.NotificationConstants;

@Component
public class PushListener {

	
	@RabbitListener(queues = NotificationConstants.QUEUE_PUSH)
	public void processPush(String content) throws InterruptedException {
		System.out.println("Process push message: " + content);
		Thread.sleep(2000L);
		throw new RuntimeException("simulate process failed");
	}
}
