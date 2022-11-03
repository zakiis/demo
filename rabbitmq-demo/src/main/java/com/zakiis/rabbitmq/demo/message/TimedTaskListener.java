package com.zakiis.rabbitmq.demo.message;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.zakiis.rabbitmq.demo.domain.constant.TimedTaskConstants;

@Component
public class TimedTaskListener {

	@RabbitListener(queues = TimedTaskConstants.QUEUE_RCMS
			, containerFactory = "timedTaskRabbitListenerContainerFactory")
	public void processSms(String content) {
		System.out.println("Process timed task message: " + content);
	}
}
