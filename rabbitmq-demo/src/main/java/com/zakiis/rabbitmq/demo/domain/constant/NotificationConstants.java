package com.zakiis.rabbitmq.demo.domain.constant;

public interface NotificationConstants {

	final String EXCHANGE_NOTIFICATION = "notification";
	final String EXCHANGE_NOTIFICATION_DLX = "notification_dlx";
	
	final String QUEUE_PUSH = "notification.push";
	final String QUEUE_PUSH_DLX = "notification.push_dlx";
	
	final String QUEUE_SMS = "notification.sms";
	final String QUEUE_SMS_DLX = "notification.sms_dlx";
	
	final String ROUTING_KEY_PUSH = "push";
	final String ROUTING_KEY_SMS = "sms";
	
	final int DELIVERY_LIMIT = 3;
	final int MAX_LENGTH_PUSH_QUEUE = 10000000;
	final int MAX_LENGTH_SMS_QUEUE = 1000000;
}
