package com.zakiis.rabbitmq.demo.domain.constant;

public interface TimedTaskConstants {

	final String EXCHANGE_TIMED_TASK = "timed-task";
	final String QUEUE_RCMS = "timed-task.rcms";
	final String ROUTING_KEY_RCMS = "rcms";
	
	final int MAX_LENGTH_RCMS = 100;
	
}
