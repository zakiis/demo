package com.zakiis.rabbitmq.demo.message;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.zakiis.rabbitmq.demo.domain.constant.NotificationConstants;

@Component
public class SmsListener {

	@RabbitListener(queues = NotificationConstants.QUEUE_SMS)
	public void processSms(String content) {
		System.out.println("Process sms message: " + content);
	}
}
