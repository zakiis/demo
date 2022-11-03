package com.zakiis.rabbitmq.demo.message.dlx;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.zakiis.rabbitmq.demo.domain.constant.NotificationConstants;

@Component
public class SmsDlxListener {

	@RabbitListener(queues = NotificationConstants.QUEUE_SMS_DLX)
	public void processPush(String content) throws InterruptedException {
		System.out.println("Sms dead letter exchange received a message: " + content);
	}
}
