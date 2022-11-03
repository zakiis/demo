package com.zakiis.rabbitmq.demo.message.dlx;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.zakiis.rabbitmq.demo.domain.constant.NotificationConstants;

@Component
public class PushDlxListener {

	@RabbitListener(queues = NotificationConstants.QUEUE_PUSH_DLX)
	public void processPush(String content) throws InterruptedException {
		System.out.println("Push dead letter exchange received a message: " + content);
	}
}
